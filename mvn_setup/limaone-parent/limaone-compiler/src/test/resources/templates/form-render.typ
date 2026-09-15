// =============================================================
// form-render.typ — Renders a Dialob questionnaire printout (the
// "mifid"-style body) to PDF. Generic over any completed Dialob form.
//
// Input (doc-data) is the flat data document of the COMPLETED form
// instance — i.e. GET /questionnaires/{id}/session-state — which is exactly
// the object POP used to nest under data.mifid, plus the eveli task data:
//   doc-data = {
//     id, metadata, formMetadata, contextValues,
//     form:   { pages: [pageId, ...] },
//     pages:  { byId: { pageId: { label, groupIds, hiddenPrint } } },
//     groups: { byId: { groupId: { label, itemIds, hiddenPrint } } },
//     items:  { byId: { itemId:  { type, label, key, value, hiddenPrint } } },
//     task:   { taskRef, subject, status, ..., customerName?, customerSsn?, comments? },
//   }
// =============================================================

#import "form-theme.typ": *


// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

#let _shown(node) = not node.at("hiddenPrint", default: false)

#let _blank(s) = s == none or (type(s) == str and s.trim() == "")

#let _yesno(v, locale: "fi") = yes-no(locale, v)

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

// Display text of one answer value by Dialob item type.
#let _value-text(item, locale: "fi") = {
  let kind = item.at("type", default: "")
  let raw = item.at("value", default: none)
  if raw == none { return "" }
  if kind == "boolean" {
    _yesno(raw, locale: locale)
  } else if kind == "number" {
    if type(raw) == int or type(raw) == float { _fmt-int(raw) } else { str(raw) }
  } else if kind == "date" {
    fmt-date(str(raw))
  } else if kind == "multichoice" {
    let vals = if type(raw) == array { raw } else { (raw,) }
    vals.filter(x => x != none).map(x => str(x)).join(", ")
  } else {
    str(raw)
  }
}


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
// NOTE RENDERING — markdown-lite
// Handles: # h1, ## h2, ### h3, #### h4, ##### h5 (kv-row style),
//          - and * bullets, 1. numbered items, **bold** inline,
//          blank lines as spacing, [["…image-path…"]] tokens are skipped.
// ─────────────────────────────────────────────────────────────

#let _render-note(body) = {
  if body == none or body == "" { return }
  // Legacy image-marker notes ("[[...]]") carry no text, skip them.
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
    } else if line.starts-with("# ") {
      v(0.4em)
      text(11.5pt, weight: "bold")[#line.slice(2)]
      v(0.2em)
    } else if line.starts-with("- ") or line.starts-with("* ") {
      in-list = true
      grid(
        columns: (10pt, 1fr),
        gutter: 4pt,
        align(top + right)[•],
        _render-inline(line.slice(2)),
      )
    } else if line.matches(regex("^[0-9]+\\. ")).len() > 0 {
      in-list = true
      let idx = line.position(". ")
      grid(
        columns: (16pt, 1fr),
        gutter: 4pt,
        align(top + right)[#line.slice(0, idx + 1)],
        _render-inline(line.slice(idx + 2)),
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

#let _render-item(item, id: "", locale: "fi") = {
  if not _shown(item) { return }
  let kind = item.at("type", default: "")
  let label = item.at("label", default: "")
  if label == none { label = "" }

  if kind == "note" {
    _render-note(label)
  } else if kind == "boolean" {
    data-row(label + ":", _yesno(item.at("value", default: none), locale: locale))
  } else if kind == "number" {
    // Values arrive raw from Dialob; format numerics with thin-space separators.
    data-row(label + ":", _value-text(item, locale: locale))
  } else if kind == "date" {
    data-row(label + ":", _value-text(item, locale: locale))
  } else if kind == "decimal" or kind == "text" or kind == "list" or kind == "time" {
    data-row(label + ":", _value-text(item, locale: locale))
  } else if kind == "multichoice" {
    let vals = item.at("value", default: ())
    if type(vals) != array { vals = (vals,) }
    [#label:]
    linebreak()
    for v in vals.filter(x => x != none) {
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

#let _render-rowgroup(items-by-id, root-id, locale: "fi") = {
  let root = items-by-id.at(root-id, default: (:))
  if not _shown(root) { return }
  let instances = _rowgroup-instances(items-by-id, root-id)
  if instances.len() == 0 { return }

  // Use the first instance to discover columns + headers.
  let first = items-by-id.at(instances.at(0), default: (:))
  let cell-ids = first.at("key", default: ())
  if type(cell-ids) != array or cell-ids.len() == 0 { return }

  let headers = cell-ids.map(cid => {
    let l = items-by-id.at(cid, default: (:)).at("label", default: "")
    if l == none { "" } else { l }
  })

  // Build rows.
  let rows = ()
  for inst-id in instances {
    let inst = items-by-id.at(inst-id, default: (:))
    if not _shown(inst) { continue }
    let inst-cells = inst.at("key", default: ())
    if type(inst-cells) != array { continue }
    let row = inst-cells.map(cid => _value-text(items-by-id.at(cid, default: (:)), locale: locale))
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

#let _render-group(mifid, group-id, depth: 1, locale: "fi") = {
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
        _render-rowgroup(items-by-id, cid, locale: locale)
      } else {
        _render-item(it, id: cid, locale: locale)
      }
    } else if cid in groups-by-id {
      _render-group(mifid, cid, depth: depth + 1, locale: locale)
    }
  }
}


// ─────────────────────────────────────────────────────────────
// PAGE RENDERING
// ─────────────────────────────────────────────────────────────

#let _render-page(mifid, page-id, locale: "fi") = {
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
      _render-rowgroup(items-by-id, gid, locale: locale)
    } else if gid in items-by-id and gid not in groups-by-id {
      _render-item(items-by-id.at(gid), id: gid, locale: locale)
    } else {
      _render-group(mifid, gid, locale: locale)
    }
  }
}


// ─────────────────────────────────────────────────────────────
// TASK SUMMARY — task and form data as data-rows (first page)
// ─────────────────────────────────────────────────────────────

#let _render-summary(mifid, locale: "fi") = {
  let meta = mifid.at("metadata", default: (:))
  let form-meta = mifid.at("formMetadata", default: (:))
  let task = mifid.at("task", default: (:))

  let rows = (
    ("reference", task.at("taskRef", default: none)),
    ("subject", task.at("subject", default: none)),
    ("status", t-enum(locale, "status-values", task.at("status", default: none))),
    ("priority", t-enum(locale, "priority-values", task.at("priority", default: none))),
    ("created", fmt-datetime(task.at("created", default: none))),
    ("due-date", fmt-date(task.at("dueDate", default: none))),
    ("assigned", task.at("assignedUser", default: none)),
    ("customer", task.at("customerName", default: none)),
    ("customer-ssn", task.at("customerSsn", default: none)),
    ("form", form-meta.at("label", default: meta.at("label", default: none))),
    ("form-submitted", fmt-datetime(meta.at("lastAnswer", default: none))),
    ("form-language", t-enum(locale, "language-values", meta.at("language", default: none))),
  ).filter(((key, value)) => not _blank(value))

  if rows.len() == 0 { return }
  for (key, value) in rows {
    data-row(t(locale, key) + ":", value)
  }
  v(0.5em)
}


// ─────────────────────────────────────────────────────────────
// CUSTOMER MESSAGES — external task comments, when requested
// ─────────────────────────────────────────────────────────────

#let _render-comments(mifid, locale: "fi") = {
  let comments = mifid.at("task", default: (:)).at("comments", default: none)
  if comments == none or type(comments) != array or comments.len() == 0 { return }

  section-title(upper(t(locale, "messages")))
  table(
    columns: (auto, auto, 1fr),
    stroke: none,
    inset: (x: 6pt, y: 4pt),
    text(weight: "bold")[#t(locale, "message-date")],
    text(weight: "bold")[#t(locale, "message-author")],
    text(weight: "bold")[#t(locale, "message-text")],
    ..comments.map(c => {
      let author = c.at("userName", default: none)
      let source = t-enum(locale, "source-values", c.at("source", default: none))
      let by = if _blank(author) { source } else if _blank(source) { author } else { author + " (" + source + ")" }
      let message = c.at("commentText", default: "")
      if message == none { message = "" }
      ([#fmt-datetime(c.at("created", default: none))], [#by], [#message])
    }).flatten(),
  )
}


// ─────────────────────────────────────────────────────────────
// DOCUMENT RENDERER
// ─────────────────────────────────────────────────────────────

#let render-document(doc-data, locale: "fi") = {
  // `doc-data` IS the Dialob printout body (the former `data.mifid` object).
  let mifid = doc-data

  _render-summary(mifid, locale: locale)

  let page-ids = mifid.at("form", default: (:)).at("pages", default: ())
  if page-ids.len() == 0 {
    // Fall back to the canonical page order.
    page-ids = mifid.at("pages", default: (:)).at("pageIds", default: ())
  }

  for (i, pid) in page-ids.enumerate() {
    if i > 0 { pagebreak() }
    _render-page(mifid, pid, locale: locale)
  }

  _render-comments(mifid, locale: locale)
}
