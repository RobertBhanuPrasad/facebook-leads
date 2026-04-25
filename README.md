# Facebook Lead Ads → CRM Integration

## Project Status: Integrated with Google Sheets

### DONE
1. **Google Sheets Integration**: Successfully implemented Google Sheets API v4 integration.
2. **Service Account Auth**: Authentication via service account JSON credentials.
3. **Auto-Sync**: Background task polls the Google Sheet every 1 minute for new leads.
4. **Manual Sync**: Added `POST /sync/google-leads` endpoint for manual triggers.
5. **Deduplication**: Prevented duplicate lead insertion using Facebook Lead ID.
6. **UI Update**: Enhanced Leads dashboard with Source badges and details.
7. **Logging & Errors**: Robust logging for sync success and failures.

### Technical Details
- **Namespace**: `in.facebookleads.google-sheets`
- **Background Job**: Configured in `src/in/facebookleads.clj` using Biff's scheduled tasks.
- **Deduplication Key**: `:lead/facebook-lead-id`
- **Primary Source**: Google Sheets (Fallback for Meta Webhooks)