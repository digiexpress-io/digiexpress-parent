# Hash Calculation Formulas

## Mathematical Definitions

### Blob Hash
```
H(blob) = μ(blob_value)
```

### Props Hash
```
H(props) = μ(props_labels ⊕ props_comments ⊕ props_permissions ⊕ props_flags)
```

### Node Hash
```
H(node) = μ(node_path ⊕ node_name ⊕ H(blob) ⊕ H(props))
```

### Tree Hash
```
H(tree) = μ(∑ᵢ₌₁ⁿ H(nodeᵢ))
```

### Commit Hash
```
H(commit) = μ(H(tree) ⊕ H(parent) ⊕ H(merge) ⊕ author ⊕ timestamp ⊕ message)
```

## Legend

- `μ` = murmur3_128 hash function
- `⊕` = concatenation operator  
- `∑` = sum over all nodes in tree (sorted by path+name)
- `n` = number of nodes in tree
- `H(x)` = hash of object x

## Hash Dependencies

```
H(commit) → H(tree) → H(node) → H(blob), H(props)
```

This creates a content-addressable storage system where any change in file content, metadata, or structure produces a different commit hash.

## Notes

- **Blob Type Exclusion**: `blob_type` is stored for debugging/filtering but excluded from hash calculation to maximize content deduplication
- **Content-Based Hashing**: Only actual content affects hash values, not descriptive metadata
- **Hash Function**: Uses Murmur3_128 for fast, non-cryptographic hashing suitable for content addressing