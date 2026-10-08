# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Primary user: the staff of a vehicle dealership in Cartagena who needs to see at
a glance which vehicles are in the lot, which ones are sold, and which ones are
in service, and who needs to register a sale without losing the vehicle to a
double sale.

Secondary context: this is the deliverable of an academic software analysis and
design course. The page is graded as evidence that the API behind it works.

## Product Purpose

The system manages a dealership's inventory: clients, vehicles, sales and
service history. Success means a salesperson can open the page, find an
available vehicle, register its sale, and know that no one else can sell the
same vehicle while they do.

## Positioning

The differentiator is a single-vehicle-at-a-time guarantee: a vehicle leaves the
available list the instant its sale is recorded, and a sale cannot be recorded
twice for the same vehicle. The page exists to make that guarantee visible, not
just enforced on the server.

## Operating Context

Spanish-language interface, Colombian market: prices in Colombian pesos, vehicle
plates in the local format, dates in `dd/mm/aaaa`. Currency conversion to US
dollars is presented alongside local prices because the dealership quotes in
both.

The page is served by the same Spring Boot application that exposes the API.
There is no separate front-end build, no framework, and no bundler: the assets
are plain files in `src/main/resources/static`.

## Capabilities and Constraints

- Read and write clients, vehicles, sales and service records.
- Vehicles move between `DISPONIBLE`, `EN_MANTENIMIENTO` and `VENDIDO`.
  `VENDIDO` is terminal.
- Prices may never be negative; vehicle plates and client emails are unique.
- A 5 % discount applies above a fixed peso threshold.
- The external exchange-rate API may be unreachable. Reads degrade to a
  fallback rate; sales are still recorded but store no dollar figure.
- The API, its 23 endpoints, its 51 Postman requests and its 209 assertions are
  verified and must keep passing. The rebrand is a **presentation-layer** change:
  entity names, endpoints and schema stay exactly as they are.
- No workflow defaults are recorded: the project has no image generation, so the
  visual work is code-first and there is nothing to choose.

## Brand Commitments

Binding, confirmed by the user:

- **Name:** Concesionaria de Cartagena.
- **Logo:** the Ferrari shield, supplied as
  `C:\Users\josea\Pictures\629bf3eeb1e6915e23d8d0285b23ecf0.jpg` and cropped to
  `static/img/escudo.png` (197 x 260, transparent).
- **Palette:** Ferrari red and yellow. Darker green for the sold state. A white
  base is retained but must look refined rather than flat.
- **Catalogue:** the products are Ferraris.
- **Language:** no wording may allude to a repair workshop. Every tab, heading,
  label, note and footer line was reviewed for it.

The shield is a registered trademark of Ferrari S.p.A. It is used here as a
reference for an academic exercise with no commercial intent. This is not a
licence, and the identity must not be reused for a commercial deployment.

## Evidence on Hand

- The brand shield, cropped, at `src/main/resources/static/img/escudo.png`.
- Seed data the user specified: Karen Lucia Zapata Castaño, Jose Casiani,
  Yanileth Echeverria, Jose Blanco.
- 209 green Postman assertions and 13 green JVM tests as the regression net for
  any change.

No product photography exists. None may be fabricated: if the page later needs
vehicle imagery, it has to come from a real source or not be used.

## Product Principles

1. **The inventory state is the truth, and it is visible.** A visitor must be
   able to tell, without clicking, what can be sold and what cannot.
2. **Never let a sale be lost.** A third-party failure may degrade a price; it
   may not destroy a transaction.
3. **The page and the API are one system.** No rule lives only in the browser.
4. **Say what happened, in plain Spanish.** Errors name the field or the rule
   that stopped the action.
5. **Respect the brand without decorating noise.** The crest and the red and
   yellow carry the identity; the interface stays quiet so the data reads.

## Accessibility & Inclusion

None established beyond the web baseline. Text contrast must stay legible on
red and yellow surfaces, which are the brand's loudest colours and its easiest
way to fail.