# AskFaculty — Google Play Checklist

Policies change. Before each submission, re-check the current rules in Play Console Help. Items marked (verify) are the ones most likely to have changed.

## 1. Account and timeline

- [ ] Choose the developer account type.
    - **Personal account created after 13 Nov 2023:** you must run a closed test with at least 12 testers who stay opted in for 14 continuous days before you can apply for production access. The 14 days cannot be shortened. If Google rejects the production application, the closed test can restart.
    - **Organization account:** exempt from that rule but needs a D-U-N-S number (can take weeks). If the college can publish under its own organization account, that is also better for trust.
- [ ] Plan about 3 weeks for the first launch on a personal account: 14 days closed test, production application, then review (usually within about a week).
- [ ] Start the closed test as soon as a stable version exists. Do not wait for every feature.
- [ ] Recruit 15–20 testers (classmates, one or two teachers) so you still have 12 if some drop out. Each tester must stay opted in for the whole period and open the app.

## 2. Policy items that apply to AskFaculty

### Account deletion (User Data policy)
- [ ] The app lets people sign in with accounts, so give users a way to request account and data deletion.
- [ ] Provide a **public web page** where the same request can be made (also usable after uninstalling). A form or a support email address is acceptable. Make the deletion section easy to find.
- [ ] Put the web link in the Data safety form.
- [ ] Admin-created accounts are a grey area. The cheapest safe path is to comply fully.
- [ ] Decide what deletion means: delete or anonymize personal data, keep academic content with the author shown as "Deleted user". Write this in the privacy policy.

### User-generated content
Students post doubts, replies, chat messages and files, so:
- [ ] In-app way to **report** content (doubts, replies, notes, chat messages).
- [ ] In-app way to **block or mute** abusive users.
- [ ] A moderation process with named responsibility (faculty and admin), published contact details, and published terms of use.
- [ ] Hidden or removed content is actually removed from other users' views.

### Privacy policy
- [ ] Publish a privacy policy on a public, non-editable URL and link it in the Play listing and inside the app.
- [ ] It must match reality: what data is collected (name, email, roll or employee ID, messages, doubts, files, notification data), why, who can see it (private doubts only to participants), where it is stored, retention, deletion, and a contact.
- [ ] If you later add analytics, crash reporting or push (FCM), update the policy and the Data safety form first.

### Data safety form
- [ ] Declare: personal info (name, email, IDs), messages and user content, files and docs, app activity if any.
- [ ] Data is encrypted in transit (HTTPS) — only say so once HTTPS is really enforced.
- [ ] Say that users can request deletion (link above).
- [ ] No data sold, no ads. Recheck after adding any SDK.

### App access (reviewer login)
- [ ] The app needs a login, so give Google reviewers working credentials in Play Console "App access".
- [ ] Create a dedicated reviewer account on the real hosted backend with believable sample data (groups, a doubt thread, a note). Never run the dev seed in production.
- [ ] Keep the reviewer account working while the app is under review. If the email flow is needed, make sure it does not block the reviewer.

### Target audience and content rating
- [ ] Set the target audience age group honestly (college students; choose 18+ if the college is adults only). Apps aimed at children have extra rules. (verify)
- [ ] Complete the content rating questionnaire, noting that users can share text and files with each other.

### Permissions
- [ ] Ask only for what you use. Use the system photo/file picker, not broad storage or media permissions.
- [ ] If push is added later, request the notification permission only at a sensible moment and explain why.

### Ads, payments, SDKs
- [ ] No ads, no payments. Declare that in Play Console.
- [ ] Avoid unnecessary third-party SDKs; each one affects the Data safety form.

## 3. Technical requirements

- [ ] Target API level meets the current Play requirement (verify in Play Console; recent guidance says new apps and updates must target API 36 from late August 2026).
- [ ] Release build: signed Android App Bundle (AAB), Play App Signing enabled, R8/minify on, not debuggable.
- [ ] HTTPS only in release. The backend is hosted behind HTTPS; cleartext allowed only in debug.
- [ ] Presigned upload URLs from object storage must be HTTPS and reachable from users' phones (not an internal hostname).
- [ ] Backups do not include tokens.
- [ ] No crashes or ANRs on the Play pre-launch report; test on a low-end phone and a few Android versions.
- [ ] Accessibility basics: touch targets, contrast, content descriptions, font scaling.

## 4. Store listing

- [ ] App name, short and full description that match what the app really does (no keyword stuffing, no claims about features that are not there).
- [ ] Icon, feature graphic, at least 2 phone screenshots (use the demo data, no real student names).
- [ ] Contact email, privacy policy URL, and the account deletion URL.
- [ ] Mention in the description that accounts are created by the college.

## 5. Common rejection reasons for this kind of app

| Reason | How to avoid |
|---|---|
| Reviewer cannot log in | Working demo credentials in App access |
| No account deletion path or web link | Build it and add the URL |
| UGC without report and block | Build report + block before submission |
| Privacy policy missing or inconsistent | Publish it, match it to the Data safety form |
| Data safety form mismatch | Re-check after every SDK or feature change |
| Crashes on review devices | Test release builds, check the pre-launch report |
| Misleading listing | Describe only what is built |
| Production access rejected (closed test) | Keep 12+ active testers, show real feedback and fixes |

## 6. If the app is rejected

1. Read the exact policy reason in the email and Play Console.
2. Fix only what is cited, update PROGRESS.md, and resubmit with a short note on what changed.
3. Repeated unfixed submissions can lead to account-level problems, so test before each submission instead of resubmitting blindly.
4. If production access is rejected after the closed test, address the questions Google raises. The closed testing period may need to run again.

## 7. Final pre-submission gate

- [ ] Report, block, deletion, privacy policy and reviewer account all verified on a release build
- [ ] Data safety form re-read against the final app
- [ ] Every item in Section 3 ticked
- [ ] Closed test finished with 12+ continuous testers (personal accounts)