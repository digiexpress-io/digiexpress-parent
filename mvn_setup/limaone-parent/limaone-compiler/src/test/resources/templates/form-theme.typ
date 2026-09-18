// =============================================================
// form-theme.typ
// =============================================================
// Register this file in Tagomi as a text/* resource named "form-theme".
// Import in any template with:
//   #import "form-theme.typ": *
//
// Apply document-wide page setup at the top of your main template:
//   #show: pop-setup.with(date: "03.03.2026", title: "Lomake")
//
// Attach the image resource "digiexpress-logo" (png) to the printout page to
// get the logo into the footer.
// =============================================================

// Image resources attached to the printout page (bytes after the host's
// __base64__ → bytes mapping), keyed by resource name.
#let _resources     = sys.inputs.at("resources", default: (:))
#let logo           = _resources.at("digiexpress-logo", default: none)


// ---- Task printout texts ------------------------------------
// Texts of the task questionnaire printout per questionnaire language,
// english is the fallback.
#let strings = (
  en: (
    yes: "Yes",
    no: "No",
    empty: "-",
    reference: "Task reference",
    subject: "Subject",
    status: "Status",
    priority: "Priority",
    created: "Task created",
    due-date: "Due date",
    assigned: "Assigned to",
    form: "Form",
    form-submitted: "Form submitted",
    form-language: "Form language",
    questionnaire: "Questionnaire",
    customer: "Customer",
    customer-ssn: "Customer identifier",
    messages: "Customer messages",
    message-date: "Date",
    message-author: "Author",
    message-text: "Message",
    status-values: (
      NEW: "New", OPEN: "Open", COMPLETED: "Completed", TRANSFERRED: "Transferred",
      REJECTED: "Rejected", DELEGATED: "Delegated", WAITING: "Waiting",
    ),
    priority-values: (LOW: "Low", NORMAL: "Normal", HIGH: "High"),
    source-values: (FRONTDESK: "Case worker", PORTAL: "Customer"),
    language-values: (fi: "Finnish", en: "English", sv: "Swedish"),
  ),
  fi: (
    yes: "Kyllä",
    no: "Ei",
    empty: "-",
    reference: "Tehtävän viite",
    subject: "Aihe",
    status: "Tila",
    priority: "Prioriteetti",
    created: "Tehtävä luotu",
    due-date: "Määräpäivä",
    assigned: "Käsittelijä",
    form: "Lomake",
    form-submitted: "Lomake lähetetty",
    form-language: "Lomakkeen kieli",
    questionnaire: "Lomake",
    customer: "Asiakas",
    customer-ssn: "Asiakkaan tunnus",
    messages: "Asiakkaan viestit",
    message-date: "Päivämäärä",
    message-author: "Lähettäjä",
    message-text: "Viesti",
    status-values: (
      NEW: "Uusi", OPEN: "Avoin", COMPLETED: "Valmis", TRANSFERRED: "Siirretty",
      REJECTED: "Hylätty", DELEGATED: "Delegoitu", WAITING: "Odottaa",
    ),
    priority-values: (LOW: "Matala", NORMAL: "Normaali", HIGH: "Korkea"),
    source-values: (FRONTDESK: "Käsittelijä", PORTAL: "Asiakas"),
    language-values: (fi: "suomi", en: "englanti", sv: "ruotsi"),
  ),
  sv: (
    yes: "Ja",
    no: "Nej",
    empty: "-",
    reference: "Uppgiftens referens",
    subject: "Ämne",
    status: "Status",
    priority: "Prioritet",
    created: "Uppgiften skapad",
    due-date: "Förfallodag",
    assigned: "Handläggare",
    form: "Formulär",
    form-submitted: "Formuläret skickat",
    form-language: "Formulärets språk",
    questionnaire: "Formulär",
    customer: "Kund",
    customer-ssn: "Kundens identifierare",
    messages: "Kundens meddelanden",
    message-date: "Datum",
    message-author: "Avsändare",
    message-text: "Meddelande",
    status-values: (
      NEW: "Ny", OPEN: "Öppen", COMPLETED: "Slutförd", TRANSFERRED: "Överförd",
      REJECTED: "Avvisad", DELEGATED: "Delegerad", WAITING: "Väntar",
    ),
    priority-values: (LOW: "Låg", NORMAL: "Normal", HIGH: "Hög"),
    source-values: (FRONTDESK: "Handläggare", PORTAL: "Kund"),
    language-values: (fi: "finska", en: "engelska", sv: "svenska"),
  ),
)

#let _strings(locale) = strings.at(locale, default: strings.en)

#let t(locale, key) = {
  let v = _strings(locale).at(key, default: none)
  if v == none { v = strings.en.at(key, default: key) }
  v
}

#let t-enum(locale, group, value) = {
  if value == none { return "" }
  let values = _strings(locale).at(group, default: (:))
  let v = values.at(str(value), default: none)
  if v == none { v = strings.en.at(group, default: (:)).at(str(value), default: str(value)) }
  v
}

#let yes-no(locale, v) = {
  if v == true { t(locale, "yes") }
  else if v == false { t(locale, "no") }
  else { t(locale, "empty") }
}

// ISO "YYYY-MM-DD…" → "DD.MM.YYYY" (+ " HH:MM" for date-times)
#let fmt-date(iso) = {
  if type(iso) != str or iso.len() < 10 { return "" }
  iso.slice(8, 10) + "." + iso.slice(5, 7) + "." + iso.slice(0, 4)
}

#let fmt-datetime(iso) = {
  if type(iso) != str or iso.len() < 16 { return fmt-date(iso) }
  fmt-date(iso) + " " + iso.slice(11, 16)
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

// =============================================================
// DOCUMENT SETUP
// Use with: #show: pop-setup.with(date: "03.03.2026", title: "Lomake")
// Parameters:
//   date       — date string shown in header, e.g. "03.03.2026"
//   title      — header title
// =============================================================
#let pop-setup(date: "03.03.2026", title: none, body) = {
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
            text(weight: "bold")[#upper(if title == none { "" } else { title })],
            text[#date #h(6pt) #pg / #total],
          )
        ]
      )
      v(6pt)
    },
    footer: [
      #line(length: 100%, stroke: 0.5pt + pop-border)
      #v(4pt)
      #if logo != none { align(right)[#image(logo, format: "png", height: 14pt)] }
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
