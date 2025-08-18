#!/bin/bash

# ============================================================================
# BUILD SUMMARY GENERATOR
# ============================================================================

set -euo pipefail

# Single stylish icon
readonly ICON="▸"

# Find all gitlog.json files
find_gitlog_files() {
    find . -name "gitlog.json" -path "*/dist/*" | sort
}

# Convert bytes to human readable format
format_file_size() {
    local bytes=$1
    local size=""
    
    if [ $bytes -lt 1024 ]; then
        size="${bytes}B"
    elif [ $bytes -lt 1048576 ]; then
        size="$(( bytes / 1024 ))KB"
    elif [ $bytes -lt 1073741824 ]; then
        size="$(( bytes / 1048576 ))MB"
    else
        size="$(( bytes / 1073741824 ))GB"
    fi
    
    echo "$size"
}

# Generate a sparkline from commit count
generate_sparkline() {
    local count=$1
    local sparkline=""
    
    if [ $count -eq 0 ]; then
        sparkline="▁"
    elif [ $count -le 3 ]; then
        sparkline="▃"
    elif [ $count -le 7 ]; then
        sparkline="▅"
    elif [ $count -le 15 ]; then
        sparkline="▇"
    else
        sparkline="█"
    fi
    
    echo "$sparkline"
}

# Create clean header
create_header() {
    echo ""
}

# Process a single module's gitlog
process_module() {
    local gitlog_file=$1
    local module_dir=$(dirname "$gitlog_file")
    local module_name=$(basename "$(dirname "$module_dir")")
    
    echo "## ${ICON} \`$module_name\`"
    echo ""
    
    # Module stats
    local commit_count=$(jq '.moduleLogs | length' "$gitlog_file" 2>/dev/null || echo "0")
    local sparkline=$(generate_sparkline $commit_count)
    
    echo "| Metric | Value |"
    echo "|--------|-------|"
    echo "| **Commits** | $commit_count $sparkline |"
    
    # Find generated files in dist
    if [ -d "$module_dir" ]; then
        local total_files=$(find "$module_dir" -type f \( -name "*.js" -o -name "*.d.ts" -o -name "*.css" -o -name "*.map" \) | wc -l)
        local total_size=0
        
        # Calculate total size
        while IFS= read -r -d '' file; do
            if [ -f "$file" ]; then
                local file_size=$(stat -c%s "$file" 2>/dev/null || echo "0")
                total_size=$((total_size + file_size))
            fi
        done < <(find "$module_dir" -type f \( -name "*.js" -o -name "*.d.ts" -o -name "*.css" -o -name "*.map" \) -print0)
        
        local formatted_size=$(format_file_size $total_size)
        echo "| **Files** | $total_files |"
        echo "| **Size** | $formatted_size |"
    fi
    
    echo ""
    
    # Commit History Section
    if [ $commit_count -gt 0 ]; then
        echo "<details>"
        echo "<summary><strong>Recent Changes</strong> ($commit_count commits)</summary>"
        echo ""
        
        # Process commits from gitlog.json
        jq -r '.moduleLogs[] | 
            "### [" + .hash[0:7] + "](https://github.com/digiexpress-io/digiexpress-parent/commit/" + .hash + ") - **" + .comment + "**\n" + 
            "> Author: **" + .author + "** | Date: **" + .date + "**\n" + 
            (if .issueId != "" then "> Issue: [#" + .issueId + "](https://github.com/digiexpress-io/digiexpress-parent/issues/" + .issueId + ")\n" else "" end) +
            (if (.modules | length) > 0 then 
            "<details><summary>Affected Modules (" + (.modules | length | tostring) + ")</summary>\n\n" +
            (.modules | map("- `" + . + "`") | join("\n")) + "\n\n</details>\n"
            else "" end) + "\n"' "$gitlog_file" 2>/dev/null
    
        echo "</details>"
    else
        echo "> *No recent changes*"
    fi
    
    echo ""
    echo "---"
    echo ""
}

# Main execution
main() {
    local summary_file="${GITHUB_STEP_SUMMARY:-build-summary.md}"
    
    echo "Generating build summary..."
    
    # Start the summary file
    {
        create_header
        
        local total_modules=0
        local total_commits=0
        local gitlog_files=($(find_gitlog_files))
        
        # Count totals first
        for gitlog_file in "${gitlog_files[@]}"; do
            if [ -f "$gitlog_file" ]; then
                total_modules=$((total_modules + 1))
                local commit_count=$(jq '.logs | length' "$gitlog_file" 2>/dev/null || echo "0")
                total_commits=$((total_commits + commit_count))
            fi
        done
        

        
        # Process each module
        for gitlog_file in "${gitlog_files[@]}"; do
            if [ -f "$gitlog_file" ]; then
                process_module "$gitlog_file"
            fi
        done
        
        # Footer
        echo "---"
        echo ""
        echo "*\"The build has been completed. There are no alternatives.\"*"
        
    } > "$summary_file"
    
    echo "Build summary generated: $summary_file"
}

# Execute
main "$@"