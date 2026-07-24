// Entry template for the Dialob questionnaire printout.
// `service.props` is the printout BODY from GET /questionnaire/{id}/printout
// (the former data.mifid object) — no profile/customer/benefit wrapper.
#let doc-data = sys.inputs.service.props

#import "form-theme.typ": *
#import "form-render.typ": *

// Header date from the questionnaire metadata (ISO "YYYY-MM-DDT…"):
// prefer lastAnswer, fall back to created.
#let _meta = doc-data.at("metadata", default: (:))
#let _iso = _meta.at("lastAnswer", default: _meta.at("created", default: ""))
#let _header-date = if type(_iso) == str and _iso.len() >= 10 {
  _iso.slice(8, 10) + "." + _iso.slice(5, 7) + "." + _iso.slice(0, 4)
} else { "" }

#show: pop-setup.with(date: _header-date)
#render-document(doc-data)
