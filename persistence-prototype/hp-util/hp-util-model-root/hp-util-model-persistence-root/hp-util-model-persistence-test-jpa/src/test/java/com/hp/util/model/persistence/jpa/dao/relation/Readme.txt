JPA associations are all inherently unidirectional.

Lets use the following association as an example.
 ______              _____
|      |        0..*|     |
| Item |<>----------| Bid |
|______|            |_____|

As far as JPA is concerned, the association from Bid to Item is a different association than the association from Item to Bid.