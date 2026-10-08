---
version: 1
slug: "src-main-resources-static-index-html"
primary_target: "src/main/resources/static/index.html"
related_targets: []
---

scope: the single-page inventory console served at GET /
visitor mode: operate

## Audience, job, task, proof

A dealership salesperson in Cartagena needs to answer one question in the first
two seconds: what can I sell right now. Then they register a sale, or send a
vehicle to service. The data that must lead is the inventory and its state.
The proof is the page itself: 5 vehicles with real plates, real peso prices and
their exchange rate, states legible at a glance.

Constraints held: the API, its 23 endpoints, the 51 Postman requests and the 209
assertions are the regression net and are untouched. No wording may allude to a
repair workshop. Brand commitments in PRODUCT.md bind the palette and the crest.

## Chosen direction

Seed key 72f780c7, assigned candidate: a Wim Crouwel gridded type specimen.
The world contributes type, palette, density and one signature move, and
nothing else. The layout stays an ordinary inventory console with ordinary web
controls.

The signature move is **the visible construction grid**: the whole console is
laid on a modular grid whose hairlines are faintly present by default, and a
real toggle in the header reveals the armature at full strength. Plates snap to
cells. It is the one move that makes Ferrari's own visual logic - letterforms
and crest built cell by cell - legible in a working screen instead of quoted as
a decoration.

### Direction contract

THESIS: The page is a grid you can see. A dealership's inventory is a set of
discrete, countable units, and the interface says so by standing on a visible
modular armature instead of floating on soft grey. It refuses the default admin
console: one grey screen, one blue accent, generous white nothing. Ferrari red
owns the shell and the user never has to hunt for the primary action.

OWN-WORLD: A near-white warm ground (not pure #fff, not cream) with hairline
construction rules at 6% ink. Ferrari rosso #D40000 carries the header rail, the
primary button and the available state. Giallo Modena #FFD500 is the accent
plane, earned by one thing per viewport only. Near-black #16181D for text on
white; the sold state is a deep green #0B6B3A. Type is a neutral grotesk at small
sizes with tight leading, plus tabular numerals for every peso figure. Density is
high: ruled modules packed edge to edge, no default SaaS whitespace inflation.

STORY: The visitor sees a dealer floor. What is red, is sellable. What is
yellow, is in service. What is green, is gone. They register a sale in three
fields and watch the vehicle leave the available list.

FIRST VIEWPORT: A red header rail with the cropped crest at its left, the word
mark set large in the fifth margin, and the five divisions as ruled cells on the
right. Under it, the count strip: five inventory tiles, one per vehicle, each
carrying plate, model and its state badge. To the right of the strip, the
exchange rate read-out with its source. The primary action, "Registrar venta",
is a red button in the top-right of the work area, not floating in a modal. The
grid toggle sits beside it.

FORM: Crouwel grid specimen, index 6, seed 72f780c7. It supplies type, palette,
density and the construction-grid move. Navigation and controls stay standard
web components.

FINISH: unreviewed and undocumented is unfinished; this build ends with the
finish review, the verdict, DESIGN.md, and every shipping raster carrying its
provenance.
