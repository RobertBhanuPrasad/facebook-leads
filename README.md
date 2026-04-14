# Facebook Lead Ads → CRM Integration

## WHY (Business Problem)
Businesses using Facebook Lead Ads often struggle with:
* Leads staying trapped inside Facebook.
* Lack of real-time access to new leads.
* Manual, time-consuming CSV downloads.
* Slow follow-up leading to lost customer opportunities.

**Our goal**: Automatically fetch leads from Facebook, store them in a custom CRM, and allow real-time management.

## WHAT (Product)
A custom **Facebook Lead Ads → CRM Integration Platform** that:
1. Authenticates users via Facebook OAuth.
2. Allows selecting specific Facebook Pages and Business Assets.
3. Subscribes to real-time Lead events via Webhooks.
4. Automatically fetches and stores full lead details in a secure CRM dashboard.

**Tech Stack**:
* **Backend**: Clojure (Biff Framework)
* **Frontend**: HTMX (Server-rendered HTML)
* **Database**: XTDB
* **API**: Facebook Graph API & Webhooks

## HOW (Implementation Flow)
1. **User Login**: OAuth flow to grant permissions for leads and pages.
2. **Setup**: Selection of Facebook Pages to monitor.
3. **Capture**: We receive a Webhook from Facebook when a form is submitted.
4. **Enrichment**: We use the `leadgen_id` to fetch complete details (Name, Email, Phone).
5. **Display**: Leads are stored in XTDB and displayed instantly in the HTMX-powered CRM UI.

## Local Setup
1. Configure credentials in `config.env`.
2. Run `clj -M:dev` to see available commands.
3. Run `clj -M:dev dev` to start the application locally.