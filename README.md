
DeepaMehta 4 Kiezatlas - Familienportal
=======================================

The module of the Kiezatlas Orte API

Account registration and usage documentation available [here](http://api.kiezatlas.de/kiezatlas-orte-api).

### Usage & Development

See and compare to the section on [usage and development in this README](https://github.com/mukil/dm4-kiezatlas-website#usage--development).

License
-------

Thise source code is licensed under the GNU GPL 3.0. It comes with no warranty.

Version History
---------------

**0.8.2** -- Upcoming

* Added fulltext search endpoint for places (incl. filter by district)
* Added endpoint to fetch certain place information by id
* Added endpoint to handle comments on places (build on the [dm4-kiezatlas-comments](https://github.com/mukil/dm4-kiezatlas-comments) module)

**0.8.1** -- Aug 02, 2016

* Cushioned famportal request if containing an unknown xmlId
* Adapted to be compatible with DeepaMehta 4.8.2
* Editorial UI extended about authentication mechanism and cookie handling

**0.8** -- Mar 09, 2016

* Compatible with DeepaMehta 4.7

**0.7** -- Jan 17, 2016

* New Familienportal Category Tree
* Compatible with DeepaMehta 4.4.3

**0.6** -- Nov 11, 2015

* Retrieval API: include "bezirk" information in Kiezatlas objects.
* Compatible with DeepaMehta 4.4

**0.5** -- Jun 11, 2014

* Redaktionswerkzeug:
    * New Familienportal category tree.
    * Fix: Öffnungszeiten HTML is properly rendered.
* Compatible with DeepaMehta 4.3

**0.4** -- May 18, 2014

* Redaktionswerkzeug:
    * Display Kiezatlas object details (middle + right column).
    * Search Kiezatlas objects by name (right column).
    * Assign individual Kiezatlas objects (by name) to Familienportal categories.
    * Assigned Kiezatlas objects are sorted (middle column).
    * Bulk-remove Kiezatlas object assignments (by category).
    * Add und remove buttons have colored icons.
    * Layout fix: the entire window height is used.
* Compatible with DeepaMehta 4.3-SNAPSHOT

**0.3** -- Apr 29, 2014

* API for retrieving Kiezatlas objects based on Familienportal category.
* Redaktionswerkzeug:
    * Search requires at least 2 characters (reduced server load).
    * Familienportal tree: display per-category object count.
    * Kiezatlas search result: display per-category object count.
* As presented at Apr 29, 2014 at Familienbeirat.
* Compatible with DeepaMehta 4.3-SNAPSHOT

**0.2** -- Apr 10, 2014

* Redaktionswerkzeug:
    * Real Familienportal category tree (5 levels).
    * Kiezatlas categories are expandable/collapsable.
    * Individual categories can be excluded before assignment.
    * Display a loading widget while waiting for responses.
    * Fix: a logical clock avoids display of obsolete responses.
    * Fix: assigning a lot of categories works (URL doesn't get too long).
* As presented at Apr 15, 2014 at Familienbeirat.
* Compatible with DeepaMehta 4.3-SNAPSHOT

**0.1** -- Mar 26, 2014

* Create and remove assignments of Kiezatlas objects to Familienportal categories.
* As presented at Apr 2, 2014 at SozKult with Reinhilde and Malte.
* Compatible with DeepaMehta 4.3-SNAPSHOT


------------
Jörg Richter, Malte Reißig (2014 - 2016)
