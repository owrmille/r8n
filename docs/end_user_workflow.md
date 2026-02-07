PREPARATION
- register in the main website (see login.html), get a cookie
- install tampermonkey and the script

BROWSE:
option a.
- go into the site (e.g. google maps - cafe page, or cafe's instagram)
- tampermonkey script:
	- downloads selectors by domain name
	- downloads your reviews with connections to these domains using your cookie
	- applies overlays wherever reviews are available
- click overlay icon, see your weighted rating for the item
- click to go deeper, get redirected to the main website, to the reviews (see item.html) or get the details right here over source site in a small table

option b.
- open main website (see dashboard.html), open your rating (see rating.html)

CONNECT:
option a.
- get a name from the owner
- search this name
- request access (see outgoing_requests.html)

option b.
- search searchable ratings by keyword (nothing is public!!! SEARCHABLE!!!)
- request access

option c.
- see recommended ratings. they were recommended because either:
	- at least 10% items are both in your currently open and recommended ratings, and the weighted marks in their reviews differ <10%
	- or you are synced to at least two ratings from that author
- request access

option d.
- from a rating you're synced to - navigate to the author
- see the list of their searchable ratings
- request access

also:
- set trust coefficients per person, per category (not implemented yet), per rating, per review. 0 to ignore (not displaying selectors, not displaying in weighted marks, only in details table... or maybe not even there?)
- cooldown for access approval of incoming requests is 3 seconds. at least read the name of the person before approving access. also we can introduce fake access requests with a signal word in the name to extend the cooldown..?

CROWDSOURCE:
- if you see the same item mentioned anywhere else in the internet, right-click on the webpage to send a link request to the developers
- (perspective) create a selector and get approvals from other users. if someone disagrees with your selector, they can just ignore you and your ratings :)

GDPR

DRAFT CLIENT-SIDE AI MODERATION
"this should go to subjective section", "please add 'I think' to make this subjective"

AI+HUMAN SERVICE-SIDE PREPUBLISH MODERATION

DELETION
- if a review/a rating is taken down as diffamation or because you requested the deletion,  they're deleted in all ratings synced to yours.

SEO
Absolutely no access for crawlers of any kind!!!

OTHER USAGES
Politicians' approval? Factchecks? Separate marks for the doctor and for their receptionists?
