No integrity rules are created because no relations are established. Deletion is either cascaded manually 
or not allowed relatives exist.

In this example when the entity in the one-side of the relation is deleted, the many-side DAO is used
to verify there are no relatives (It could be used to delete relatives). When an entity in the many-side 
of the relation is created, the one-side DAO is used to verify the relative actually exists (This could be optional). 

It is recommended not to expose the delete method with a filter in the one-side DAO via queries since 
it is not efficient. 