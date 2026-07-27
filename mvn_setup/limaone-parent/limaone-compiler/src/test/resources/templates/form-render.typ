// =============================================================
// form-render.typ — Renders a Dialob questionnaire printout (the
// "mifid"-style body) to PDF. Generic over any completed Dialob form.
//
// Input (doc-data) is the printout BODY produced by the dialob-printout
// service — i.e. GET /questionnaire/{id}/printout — which is exactly the
// object POP used to nest under data.mifid:
//   doc-data = {
//     id, metadata, formMetadata, contextValues,
//     form:   { pages: [pageId, ...] },
//     pages:  { byId: { pageId: { label, groupIds, hiddenPrint } } },
//     groups: { byId: { groupId: { label, itemIds, hiddenPrint } } },
//     items:  { byId: { itemId:  { type, label, key, value, hiddenPrint } } },
//   }
// =============================================================

#import "form-theme.typ": *


// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

#let _shown(node) = not node.at("hiddenPrint", default: false)

#let _yesno(v) = boolean-text(v)

// Format an integer with thin-space thousands separators ("56 777" style).
#let _fmt-int(n) = {
  let v = int(n)
  let neg = v < 0
  let s = str(calc.abs(v))
  let out = ""
  let i = s.len()
  while i > 3 {
    out = " " + s.slice(i - 3, i) + out
    i = i - 3
  }
  out = s.slice(0, i) + out
  if neg { "-" + out } else { out }
}

#let _fmt-eur(n) = _fmt-int(n) + " €"


// ─────────────────────────────────────────────────────────────
// INLINE MARKUP — handles **bold** in a single line of note text
// ─────────────────────────────────────────────────────────────

#let _render-inline(s) = {
  // Split on "**" and toggle bold between segments.
  let parts = s.split("**")
  let bold = false
  for p in parts {
    if p != "" {
      if bold { text(weight: "bold")[#p] } else { [#p] }
    }
    bold = not bold
  }
}


// ─────────────────────────────────────────────────────────────
// RISK-IMAGE MARKER PARSING
//   "[[\"/risk-images/imageriskLevel1.png\"]]" → "/risk-images/…"
// Some forms serialize inline image references as a JSON-encoded nested
// array in a note label. We grab the inner path, then look up the basename
// in `images-data` (supplied by the consuming app, keyed by filename).
// ─────────────────────────────────────────────────────────────

#let _parse-image-marker(s) = {
  if s == none or type(s) != str { return none }
  let t = s
  while t.starts-with(" ")   { t = t.slice(1) }
  while t.ends-with(" ")     { t = t.slice(0, t.len() - 1) }
  if not (t.starts-with("[[\"") and t.ends-with("\"]]")) { return none }
  t.slice(3, t.len() - 3)
}

#let _render-risk-image(path, width: 70%) = {
  let parts = path.split("/")
  let name = parts.at(parts.len() - 1)
  let data = images-data.at(name, default: none)
  if data == none { return }
  v(0.4em)
  align(center)[
    #image(data, format: "png", width: width)
  ]
  v(0.4em)
}


// ─────────────────────────────────────────────────────────────
// NOTE RENDERING — markdown-lite
// Handles: ## h2, ### h3, #### h4, ##### h5 (kv-row style),
//          - bullets, **bold** inline, blank lines as spacing,
//          [["…image-path…"]] tokens are skipped.
// ─────────────────────────────────────────────────────────────

#let _render-note(body) = {
  if body == none or body == "" { return }
  // Skip image-reference notes entirely (they're handled elsewhere).
  if body.starts-with("[[") and body.ends-with("]]") { return }

  let lines = body.split("\n")
  let in-list = false
  for raw in lines {
    let line = raw
    // Trim a single leading/trailing space (cheap normalization).
    while line.starts-with(" ") { line = line.slice(1) }

    if line == "" {
      if in-list { in-list = false }
      v(0.35em)
    } else if line.starts-with("##### ") {
      // h5 used like "Label: **value**" — render as data-row when it fits.
      let rest = line.slice(6)
      let parts = rest.split(":")
      if parts.len() >= 2 {
        let lbl = parts.at(0)
        let val = parts.slice(1).join(":")
        while val.starts-with(" ") { val = val.slice(1) }
        // Strip surrounding ** from value if present.
        let v-clean = val.replace("**", "")
        data-row(lbl + ":", v-clean)
      } else {
        text(weight: "bold")[#_render-inline(rest)]
        linebreak()
      }
    } else if line.starts-with("#### ") {
      v(0.3em)
      text(9.5pt, weight: "bold")[#line.slice(5)]
      v(0.15em)
    } else if line.starts-with("### ") {
      v(0.3em)
      text(10pt, weight: "bold")[#line.slice(4)]
      v(0.15em)
    } else if line.starts-with("## ") {
      v(0.4em)
      text(11pt, weight: "bold")[#line.slice(3)]
      v(0.2em)
    } else if line.starts-with("- ") {
      in-list = true
      grid(
        columns: (10pt, 1fr),
        gutter: 4pt,
        align(top + right)[•],
        _render-inline(line.slice(2)),
      )
    } else {
      _render-inline(line)
      linebreak()
    }
  }
}


// ─────────────────────────────────────────────────────────────
// ITEM RENDERING
// ─────────────────────────────────────────────────────────────

#let _render-item(item, id: "") = {
  if not _shown(item) { return }
  let t = item.at("type", default: "")

  if t == "note" {
    // Inline image marker (e.g. a risk-level image) → render the referenced
    // PNG. Falls through to plain note rendering when the label isn't a marker.
    let img-path = _parse-image-marker(item.at("label", default: ""))
    if img-path != none {
      _render-risk-image(img-path)
      return
    }
    _render-note(item.at("label", default: ""))
  } else if t == "boolean" {
    data-row(item.at("label", default: "") + ":", _yesno(item.at("value", default: none)))
  } else if t == "number" {
    // Values arrive raw from Dialob; format numerics with thin-space separators.
    let raw = item.at("value", default: none)
    let display = if raw == none { "" } else if type(raw) == int or type(raw) == float {
      _fmt-int(raw)
    } else {
      str(raw)
    }
    data-row(item.at("label", default: "") + ":", display)
  } else if t == "decimal" or t == "text" or t == "list" {
    let v = item.at("value", default: "")
    if v == none { v = "" }
    data-row(item.at("label", default: "") + ":", str(v))
  } else if t == "multichoice" {
    let lbl = item.at("label", default: "")
    let vals = item.at("value", default: ())
    if type(vals) != array { vals = (vals,) }
    [#lbl:]
    linebreak()
    for v in vals {
      grid(
        columns: (60pt, 10pt, 1fr),
        gutter: 4pt,
        [],
        align(top + right)[•],
        [#v],
      )
    }
  }
}


// ─────────────────────────────────────────────────────────────
// ROWGROUP — table with one row per "groupN.M" instance
// ─────────────────────────────────────────────────────────────

#let _rowgroup-instances(items-by-id, root-id) = {
  // The root rowgroup item's "key" is an array of instance ids ("groupN.0", ...).
  // Each instance has its own "key" pointing at the cell items.
  let root = items-by-id.at(root-id, default: (:))
  let inst-ids = root.at("key", default: ())
  if type(inst-ids) != array { inst-ids = () }
  inst-ids
}

#let _render-rowgroup(items-by-id, root-id) = {
  let root = items-by-id.at(root-id, default: (:))
  if not _shown(root) { return }
  let instances = _rowgroup-instances(items-by-id, root-id)
  if instances.len() == 0 { return }

  // Use the first instance to discover columns + headers.
  let first = items-by-id.at(instances.at(0), default: (:))
  let cell-ids = first.at("key", default: ())
  if type(cell-ids) != array or cell-ids.len() == 0 { return }

  let headers = cell-ids.map(cid => items-by-id.at(cid, default: (:)).at("label", default: ""))

  // Build rows.
  let rows = ()
  for inst-id in instances {
    let inst = items-by-id.at(inst-id, default: (:))
    if not _shown(inst) { continue }
    let inst-cells = inst.at("key", default: ())
    if type(inst-cells) != array { continue }
    let row = inst-cells.map(cid => {
      let cell = items-by-id.at(cid, default: (:))
      let cv = cell.at("value", default: "")
      if cv == none { cv = "" }
      str(cv)
    })
    rows.push(row)
  }

  v(0.3em)
  text(weight: "bold")[#root.at("label", default: "")]
  v(0.2em)
  table(
    columns: cell-ids.map(_ => auto),
    stroke: none,
    inset: (x: 6pt, y: 4pt),
    ..headers.map(h => text(weight: "bold")[#h]),
    ..rows.flatten().map(c => [#c]),
  )
}


// ─────────────────────────────────────────────────────────────
// GROUP RENDERING (recursive)
// ─────────────────────────────────────────────────────────────

// Recursively determine whether a group has any printable content.
#let _group-has-content(mifid, group-id) = {
  let groups-by-id = mifid.at("groups", default: (:)).at("byId", default: (:))
  let items-by-id  = mifid.at("items",  default: (:)).at("byId", default: (:))
  let group = groups-by-id.at(group-id, default: (:))
  if group == (:) or not _shown(group) { return false }
  let any = false
  for cid in group.at("itemIds", default: ()) {
    if cid in items-by-id {
      let it = items-by-id.at(cid)
      if _shown(it) { any = true }
    } else if cid in groups-by-id {
      if _group-has-content(mifid, cid) { any = true }
    }
  }
  any
}

#let _render-group(mifid, group-id, depth: 1) = {
  let groups-by-id = mifid.at("groups", default: (:)).at("byId", default: (:))
  let items-by-id  = mifid.at("items",  default: (:)).at("byId", default: (:))

  let group = groups-by-id.at(group-id, default: (:))
  if group == (:) { return }
  if not _shown(group) { return }
  if not _group-has-content(mifid, group-id) { return }

  let lbl = group.at("label", default: "")
  if lbl != none and lbl != "" {
    v(0.4em)
    if depth == 1 {
      text(9.5pt, weight: "bold")[#lbl]
    } else {
      text(9pt, weight: "bold")[#lbl]
    }
    v(0.15em)
  }

  let child-ids = group.at("itemIds", default: ())
  for cid in child-ids {
    // Prefer items lookup first; fall back to nested groups.
    if cid in items-by-id {
      let it = items-by-id.at(cid)
      if it.at("type", default: "") == "rowgroup" {
        _render-rowgroup(items-by-id, cid)
      } else {
        _render-item(it, id: cid)
      }
    } else if cid in groups-by-id {
      _render-group(mifid, cid, depth: depth + 1)
    }
  }
}


// ─────────────────────────────────────────────────────────────
// PAGE RENDERING
// ─────────────────────────────────────────────────────────────

#let _render-page(mifid, page-id) = {
  let pages-by-id  = mifid.at("pages", default: (:)).at("byId", default: (:))
  let items-by-id  = mifid.at("items",  default: (:)).at("byId", default: (:))
  let groups-by-id = mifid.at("groups", default: (:)).at("byId", default: (:))

  let page = pages-by-id.at(page-id, default: (:))
  if page == (:) { return }
  if not _shown(page) { return }

  let lbl = page.at("label", default: "")
  if lbl != none and lbl != "" {
    section-title(upper(lbl))
  }

  let group-ids = page.at("groupIds", default: ())
  for gid in group-ids {
    // A page can reference an id that lives in `items.byId` (e.g. a rowgroup)
    // as well as `groups.byId`. Render the rowgroup item when the group is empty.
    let is-item-rowgroup = (
      gid in items-by-id and items-by-id.at(gid).at("type", default: "") == "rowgroup"
    )
    if is-item-rowgroup and not _group-has-content(mifid, gid) {
      _render-rowgroup(items-by-id, gid)
    } else {
      _render-group(mifid, gid)
    }
  }
}


// ─────────────────────────────────────────────────────────────
// DOCUMENT RENDERER
// ─────────────────────────────────────────────────────────────

#let render-document(doc-data) = {
  // `doc-data` IS the Dialob printout body (the former `data.mifid` object).
  let mifid = doc-data

  let page-ids = mifid.at("form", default: (:)).at("pages", default: ())
  if page-ids.len() == 0 {
    // Fall back to the canonical page order.
    page-ids = mifid.at("pages", default: (:)).at("pageIds", default: ())
  }

  for (i, pid) in page-ids.enumerate() {
    if i > 0 { pagebreak() }
    _render-page(mifid, pid)
  }
}
