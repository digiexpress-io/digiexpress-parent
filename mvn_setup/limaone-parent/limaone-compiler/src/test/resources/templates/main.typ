// Entry template for the task questionnaire printout (tagomi printout "task-pdf").
// `service.props` is the flat data document of the COMPLETED form instance
// (GET /questionnaires/{id}/session-state) plus the eveli task data under `task`.
// The same page content serves every printout locale: the texts follow the
// questionnaire language, english when there is no translation for it.
#let doc-data = sys.inputs.service.props

#import "form-theme.typ": *
#import "form-render.typ": *

#let _meta = doc-data.at("metadata", default: (:))

#let _locale = {
  let language = _meta.at("language", default: none)
  if language == none or language == "" { "en" } else { language }
}

// Header date from the questionnaire metadata (ISO "YYYY-MM-DDT…"):
// prefer lastAnswer, fall back to created.
#let _iso = _meta.at("lastAnswer", default: _meta.at("created", default: ""))
#let _header-date = if type(_iso) == str and _iso.len() >= 10 {
  _iso.slice(8, 10) + "." + _iso.slice(5, 7) + "." + _iso.slice(0, 4)
} else { "" }

// Header title from the form label.
#let _title = {
  let label = doc-data.at("formMetadata", default: (:)).at("label", default: _meta.at("label", default: none))
  if label == none or label == "" { t(_locale, "questionnaire") } else { label }
}

#show: pop-setup.with(date: _header-date, title: _title)
#render-document(doc-data, locale: _locale)
