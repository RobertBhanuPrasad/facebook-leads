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



.

🧠 2. WHY WE ARE USING NGROK (Detailed but Simple)
❓ Problem

Your app runs on:

http://localhost:8080

👉 Facebook cannot access localhost

❓ Why?

Facebook servers are on the internet
localhost = only your machine

So:

Facebook → cannot reach your backend ❌
✅ Solution → ngrok

ngrok creates a public URL for your local server

Example:

https://abc123.ngrok-free.app → http://localhost:8080
🔁 Flow with ngrok
User → Facebook login
Facebook → redirects to ngrok URL
ngrok → forwards to localhost
Your backend → processes request
🔥 Important Note

👉 ngrok URL changes every time you restart it (free plan)

⚙️ 3. HOW TO RUN NGROK (Step-by-Step)

Run this command:

ngrok http 8080 --request-header-add="ngrok-skip-browser-warning:true"
🔍 Output will look like:
Forwarding:
https://de6f-88-198-67-220.ngrok-free.app → http://localhost:8080

👉 This is your new public URL

🔧 4. WHAT TO UPDATE AFTER NGROK STARTS

You MUST update 2 places

🔹 A. META (Facebook Developer Dashboard)

Go to:

👉 Facebook App → Settings → Facebook Login → Settings

Update:
✅ Valid OAuth Redirect URIs
https://YOUR-NGROK-URL/facebook/callback

Example:

https://de6f-88-198-67-220.ngrok-free.app/facebook/callback
✅ App Domains

Go to:

👉 App Settings → Basic

Add:

de6f-88-198-67-220.ngrok-free.app
🔹 B. YOUR PROJECT (config.env)

Update:

DOMAIN=de6f-88-198-67-220.ngrok-free.app

FACEBOOK_REDIRECT_URI=https://de6f-88-198-67-220.ngrok-free.app/facebook/callback
🔁 FINAL STEP

Webhook updates:

1) enter the callback url = https://c97b-103-215-165-73.ngrok-free.app/webhook/facebook
2) enter the verify token = lead-crm-verify-token

select product - page -> subscribe to leadgen

ngrok checking urls - http://127.0.0.1:4040/inspect/http

app subscription ->  https://developers.facebook.com/tools/explorer/?method=GET&path=1102745389586149%2Fsubscribed_apps&version=v25.0

parameter -> 1102745389586149/subscribed_apps (parameter -> subscribed_fields = leadgen)

delete lead -> create new lead -> check webhook --> https://developers.facebook.com/tools/lead-ads-testing

Restart backend:

clj -M:dev dev
⚠️ IMPORTANT RULE (VERY IMPORTANT)

Every time ngrok restarts:

URL changes → you MUST update:
1. Meta Dashboard
2. config.env
🧩 FULL FLOW (FOR README)
Facebook OAuth Flow
1. User clicks "Connect with Facebook"
2. Redirect to Facebook login
3. User grants permissions
4. Facebook sends code → /facebook/callback
5. Backend exchanges code → access token
6. Token stored in DB
7. Backend fetches pages
8. User sees "Select Your Page"
🚀 NEXT DEVELOPMENT STEP

Now we will:

1. Select a Facebook Page
2. Fetch leads using Graph API
3. Store leads in database
4. Display leads in CRM UI
🧠 FINAL NOTE YOU CAN ADD
Ngrok is used only for development.
In production, we will use a fixed domain (e.g., https://app.yourdomain.com)
so no repeated configuration is required.
👍 DONE

This is:

Client-ready message ✅
README-ready explanation ✅
Dev setup guide ✅