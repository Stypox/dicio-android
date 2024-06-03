This folder contains the old implementation of the "standard" recognizer, which worked in `O(n²)`, did not treat capturing groups well, had an unstable scoring system, and was not extensible easily.

The implementation is still kept under the `test` module in order to be able to run performance tests on it and compare it with the new system.
