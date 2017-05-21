Your overall score for this assignment is 9.05 out of 10.00


The code you submitted did not pass all of our tests: your submission achieved a score of
9.05 out of 10.00 in our tests.

In order to find bugs in your code, we advise to perform the following steps:
 - Take a close look at the test output that you can find below: it should point you to
   the part of your code that has bugs.
 - Run the tests that we provide with the handout on your code.
 - The tests we provide do not test your code in depth: they are very incomplete. In order
   to test more aspects of your code, write your own unit tests.
 - Take another very careful look at the assignment description. Try to find out if you
   misunderstood parts of it. While reading through the assignment, write more tests.

Below you can find a short feedback for every individual test that failed.

======== LOG OF FAILED TESTS ========
Your solution achieved a testing score of 57 out of 63.

Below you can see a short feedback for every test that failed,
indicating the reason for the test failure and how many points
you lost for each individual test.

Tests that were aborted took too long too complete or crashed the
JVM. Such crashes can arise due to infinite non-terminitaing
loops or recursion (StackOverflowException) or excessive memory
consumption (OutOfMemoryException).

[Test Description] Leaf.insert(b) should return a new Fork if size > minimumSize
[Observed Error] nw of the Fork, Empty(17.5,27.5,5.0), should be a Leaf
[Lost Points] 2

[Test Description] Fork.insert(b) should insert recursively in the appropriate quadrant
[Observed Error] The new ne, Leaf(20.0,30.0,10.0,List(barneshut.package$Body@3911c2a7)), should be a Fork
[Lost Points] 2

[Test Description] 'insert' should work correctly on a leaf with center (1,1) and size 2
[Observed Error] Fork(Empty(0.5,0.5,1.0),Empty(1.5,0.5,1.0),Empty(0.5,1.5,1.0),Empty(1.5,1.5,1.0)) did not equal Fork(Leaf(0.5,0.5,1.0,List(barneshut.package$Body@4facf68f)),Leaf(1.5,0.5,1.0,List(barneshut.package$Body@76508ed1)),Empty(0.5,1.5,1.0),Empty(1.5,1.5,1.0)) expected Fork(Leaf(0.5,0.5,1.0,List(barneshut.package$Body@4facf68f)),Leaf(1.5,0.5,1.0,List(barneshut.package$Body@76508ed1)),Empty(0.5,1.5,1.0),Empty(1.5,1.5,1.0)) found Fork(Empty(0.5,0.5,1.0),Empty(1.5,0.5,1.0),Empty(0.5,1.5,1.0),Empty(1.5,1.5,1.0))
[Lost Points] 2

======== TESTING ENVIRONMENT ========
Limits: memory: 256m,  total time: 850s,  per test case time: 240s

======== DEBUG OUTPUT OF TESTING TOOL ========
matrix: 27 ms; avg: NaN
matrix: 12 ms; avg: NaN
matrix: 6 ms; avg: NaN
update: 3 ms; avg: NaN
update: 1 ms; avg: NaN