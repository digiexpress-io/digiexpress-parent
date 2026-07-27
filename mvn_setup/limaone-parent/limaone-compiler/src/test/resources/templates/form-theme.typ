// =============================================================
// form-theme.typ
// =============================================================
// Register this file in Tagomi as a text/* resource named "form-theme".
// Import in any template with:
//   #import "form-theme.typ": *
//
// Apply document-wide page setup at the top of your main template:
//   #show: pop-setup.with(date: "03.03.2026")
// =============================================================

// ---- i18n (labels) ------------------------------------------
// Shape mirrors the legacy handlebars labels JSON: `reco.<key>`, `form.<key>`,
// `mifid.{title,groups,items,pages}`, `boolean.{true,false}`, `complexProduct`,
// `assignment`, `documentsPresented`. Missing keys fall back to the provided
// default (or the key itself), so the document still renders even when a
// language file is incomplete (e.g. en.json lacks `complexProduct`).
#let _svc           = sys.inputs.at("service", default: (:))
#let labels         = _svc.at("labels", default: (:))
#let lang           = _svc.at("lang",   default: "fi")
// Image bytes keyed by filename, supplied by compile.sh from the risk-images/
// folder. Each value is `bytes` after the host's __base64__ → bytes mapping.
#let images-data    = _svc.at("images", default: (:))

#let _labels-reco   = labels.at("reco",   default: (:))
#let _labels-form   = labels.at("form",   default: (:))
#let _labels-mifid  = labels.at("mifid",  default: (:))
#let _labels-bool   = labels.at("boolean", default: (:))

#let reco-text(key, default: none) = {
  let v = _labels-reco.at(key, default: none)
  if v == none { if default == none { key } else { default } } else { v }
}

#let form-label(key, default: none) = {
  let v = _labels-form.at(key, default: none)
  if v == none { if default == none { key } else { default } } else { v }
}

#let mifid-title(default: "SOVELTUVUUSARVIOINTI") = {
  let v = _labels-mifid.at("title", default: none)
  if v == none { default } else { v }
}

#let mifid-group(id, default: none) = {
  let groups = _labels-mifid.at("groups", default: (:))
  let v = groups.at(id, default: none)
  if v == none { if default == none { id } else { default } } else { v }
}

#let boolean-text(v) = {
  if v == true  { _labels-bool.at("true",  default: "Kyllä") }
  else if v == false { _labels-bool.at("false", default: "Ei") }
  else { "-" }
}

#let complex-product-labels() = labels.at("complexProduct", default: none)
#let last-disclaimer-labels() = _labels-form.at("lastDisclaimer", default: ())

// Replace every "{name}" occurrence in `template` with the matching entry from
// `vars` (a dict). Returns the substituted string.
#let fmt(template, vars) = {
  let out = template
  for (k, v) in vars.pairs() {
    out = out.replace("{" + k + "}", str(v))
  }
  out
}

// For "Label text: {placeholder} €"-style templates, return the part before
// the first "{" with any trailing whitespace stripped — used as a label
// in the two-column data-row layout.
#let label-prefix(template) = {
  let i = template.position("{")
  let s = if i == none { template } else { template.slice(0, i) }
  while s.ends-with(" ") { s = s.slice(0, s.len() - 1) }
  s
}

// For the same templates, return the suffix after the closing "}" — usually
// " €". Leading whitespace is preserved so the value reads "1234 €".
#let value-suffix(template) = {
  let i = template.position("}")
  if i == none { "" } else { template.slice(i + 1) }
}


// ---- Brand palette ------------------------------------------
#let pop-green    = rgb("#00843d")
#let pop-purple   = rgb("#5b2d8e")
#let pop-dark     = rgb("#1a1a1a")
#let pop-gray     = rgb("#6b7280")
#let pop-border   = rgb("#e5e7eb")
#let pop-stripe   = rgb("#f3f4f6")
#let pop-bg-green = rgb("#ecfdf5")
#let pop-bg-warn  = rgb("#fffbeb")
#let pop-warn     = rgb("#d97706")

// ---- Allocation category palette (5 asset classes) ----------
// Used consistently across all pie charts in the document.
#let alloc-colors = (
  rgb("#f87171"),  // Lyhyt korko ja käteinen — red
  rgb("#34d399"),  // Pitkä korko             — emerald
  rgb("#818cf8"),  // Osake                   — indigo
  rgb("#059669"),  // Reaaliomaisuus          — dark green
  rgb("#a78bfa"),  // Vaihtoehtoinen          — violet
)

#let alloc-labels = (
  reco-text("chart.shortInterest",     default: "Lyhyt korko ja käteinen"),
  reco-text("chart.longInterest",      default: "Pitkä korko"),
  reco-text("chart.share",             default: "Osake"),
  reco-text("chart.realAssets",        default: "Reaaliomaisuus"),
  reco-text("chart.alternativeAssets", default: "Vaihtoehtoinen"),
)

// =============================================================
// DOCUMENT SETUP
// Use with: #show: pop-setup.with(date: "03.03.2026")
// Parameters:
//   date       — date string shown in header, e.g. "03.03.2026"
// =============================================================
#let pop-setup(date: "03.03.2026", body) = {
  set page(
    paper: "a4",
    margin: (x: 1.5cm, top: 2.8cm, bottom: 1.8cm),
    header-ascent: 25pt,
    header: context {
      let pg    = counter(page).at(here()).first()
      let total = counter(page).final().first()
      pad(x: -1.5cm,
        block(
          width: 100%,
          fill: pop-purple,
          inset: (x: 1.5cm + 8pt, y: 10pt),
          stroke: none,
        )[
          #set text(8pt, fill: white)
          #grid(
            columns: (1fr, auto),
            text(weight: "bold")[#upper(mifid-title())],
            text[#date #h(6pt) #pg / #total],
          )
        ]
      )
      v(6pt)
    },
    footer: [
      #line(length: 100%, stroke: 0.5pt + pop-border)
    ],
  )
  set text(font: "Segoe UI", size: 9pt, fill: pop-dark)
  set par(leading: 0.6em)
  body
}


// =============================================================
// HEADINGS
// =============================================================

#let section-title(title) = {
  v(0.5em)
  text(12pt, weight: "bold", fill: pop-dark)[#title]
  v(2pt)
  line(length: 100%, stroke: 0.5pt + pop-border)
  v(0.5em)
}

#let sub-title(title) = {
  v(0.3em)
  text(9.5pt, weight: "bold")[#title]
  v(0.2em)
}


// =============================================================
// DATA DISPLAY
// =============================================================

#let data-row(label, value) = {
  grid(
    columns: (58%, 42%),
    gutter: 4pt,
    text(fill: pop-gray)[#label],
    text(weight: "bold")[#value],
  )
  v(0.12em)
}

#let kv-table(..cells) = {
  table(
    columns: (1fr, auto),
    stroke: 0.4pt + pop-border,
    fill: (_, y) => if calc.odd(y) { pop-stripe },
    inset: (x: 6pt, y: 5pt),
    ..cells.pos(),
  )
}

#let summary-box(rows) = {
  block(
    fill: pop-bg-green,
    width: 100%,
    inset: (x: 12pt, y: 10pt),
    radius: 3pt,
    stroke: 0.5pt + pop-green.lighten(50%),
  )[
    #for (label, value) in rows [
      #grid(
        columns: (1fr, auto),
        text(weight: "semibold")[#label],
        text(weight: "bold", fill: pop-green)[#value],
      )
      #v(4pt)
    ]
  ]
}

#let warn-box(content) = {
  block(
    fill: pop-bg-warn,
    width: 100%,
    inset: (x: 12pt, y: 10pt),
    radius: 3pt,
    stroke: 0.5pt + pop-warn.lighten(40%),
  )[
    #text(fill: pop-warn)[#content]
  ]
}

#let check-item(content) = {
  grid(
    columns: (14pt, 1fr),
    gutter: 4pt,
    align(top)[
      #block(
        width: 10pt,
        height: 10pt,
        stroke: 0.5pt + pop-gray,
        radius: 1pt,
      )[
        #align(center + horizon)[
          #text(7pt, weight: "bold", fill: pop-green)[✓]
        ]
      ]
    ],
    content,
  )
  v(3pt)
}

#let alloc-legend-row(idx, pct, show-pct: true) = {
  if show-pct {
    grid(
      columns: (10pt, 1fr, auto),
      column-gutter: 5pt,
      block(width: 10pt, height: 10pt, fill: alloc-colors.at(idx), radius: 2pt, stroke: none),
      text(7.5pt)[#alloc-labels.at(idx)],
      text(7.5pt, weight: "bold")[#pct\%],
    )
  } else {
    grid(
      columns: (10pt, 1fr),
      column-gutter: 5pt,
      block(width: 10pt, height: 10pt, fill: alloc-colors.at(idx), radius: 2pt, stroke: none),
      text(7.5pt)[#alloc-labels.at(idx)],
    )
  }
  v(2pt)
}

// Default: show only category labels alongside the color swatches.
// Percentages now live inside each donut, so the side legend is for labels.
#let alloc-legend(data-percents, show-pct: false) = {
  for (i, pct) in data-percents.enumerate() {
    alloc-legend-row(i, pct, show-pct: show-pct)
  }
}

// Horizontal legend — colour swatches + category labels in a single row.
// Used underneath chart rows so the labels don't crowd one chart's column.
#let alloc-legend-horizontal() = {
  set text(8pt)
  align(center, stack(dir: ltr, spacing: 14pt,
    ..range(alloc-labels.len()).map(i =>
      grid(
        columns: (10pt, auto),
        column-gutter: 5pt,
        block(width: 10pt, height: 10pt, fill: alloc-colors.at(i), radius: 2pt, stroke: none),
        [#alloc-labels.at(i)],
      )
    )
  ))
}
