
## Java Configuration Library

Similar to apache commons configuration but strives to be simple and have as
little exteranl dependencies as possible.

Example usage:

```java

// Plain 'ol constructor, create a new konfiguration:
Konfiguration konfig = new JsonKonfiguration("{...JSON STRING...}");

// Get the value, notice the .v()
boolean b = konfig.bool   ("some.konfig.key.deeply.nested").v()
int     i = konfig.int_   ("some.int").v()
long    l = konfig.long_  ("some.long").v()
String  s = konfig.string ("aString").v()

List<String>         list = konfig.list("a.nice.string.list", String.class).v()
Map<String, Integer> map  = konfig.map ("my.map", int.class).v()

```

### Live updates

Konfiguration value can be updated during runtime. A konfiguration instance
does not return a value directly but returns a wrapper. The wrapper has a method
v() which returns the actual value. 

```java
// The json string is updated by some thread.
private String jsonStr;
private Supplier<String> jsonSupplier = () -> jsonStr;

// ...

// create an instance with a supplier instead of string.
Konfiguration konfig = new JsonKonfiguration(jsonSupplier);
KonfigV<Boolean> konfigValue = konfig.bool("some.konfig.key.deeply.nested");

boolean oldValue = konfigValue.v();

jsonStr = "..."; // update the configuration and toggle some.konfig.key.deeply.nested
konfig.update(); // notify the konfiguration of the update. you would want to 
                 // to do this in the thread that updated the jsonStr.

boolean newValue  = konfigValue.v();

// If you have toggled the some.konfig.key.deeply.nested, then:
assert newValuea == (!oldValue);

```

Holding a reference to the wrapper instead of the actual value, it's possible 
to get the latest updated value. There is no need to have a hold on the 
konfiguration instance or the actual konfig key. The wrapper itself is enough.

### Registering to updates:

todo


### Lists and Maps:

todo

### Custom types:

todo

### Multiple sources:

todo


### Assumptions / Limitaions:

Shit. this is all wrong

 - A config value may be updated, BUT it will not be removed from a 
   configuration source, otherwise the behaviour is not predicted.
   (reason: after an instance of KonfigV is created, it's expected to always
    return a value, and not be removed).

 - When combining multiple configuration sources, a config key is always read
   from a single source, and it is NOT moved between different sources.
   Otherwise, clients of a configuration key will NOT be notified of furthur
   change and updates.
   Multiple sources can have the same key (override each other) as long as the
   overriding key is not removed from the source containing that key.

 - Updates to configuration sources MUST, MUST, MUST take place in a single 
   thread. Also, they should not be too frequesnt (too frequent as in updateing
   in a while loop with no delay. A few milli-seconds would do).
