# Lexical-Search-Table

This is a try to extend AdaptiveTableLayout from Cleveroad by implementing a lexical search among table's entries.
This uses the sample code from Cleveroad as a basis, the modifications are mainly in SampleLinkedTableAdapter and TableLayoutFragment.

The strategy is to implement a search on a lexical dictionnary online (with jsoup), and compare results with other entries among the table. A random vector of colors is implemented and updated depending on the cases (words that share a lexical field share the same backgroung color).

NB : this work is still in progress, some bugs need to be fixed.
