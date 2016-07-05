
## Java Configuration Library

Similar to apache commons configuration but strives to be simple and have as
little exteranl dependencies as possible.

Example usage:

```

// Plain 'ol constructor, create a new konfiguration:
String json = "...";
Konfiguration konfig = new JsonKonfiguration(json);

// Get the value, notice the .v()
bool b = konfig.bool("some.konfig.key.deeply.nested").v()

// Also:
// konfig.int_(...), konfig.long_, konfig.string, konfig.list, konfig.map
```


### Live updates

Konfiguration value can be updated during runtime. A konfiguration instance
does not return a value directly but returns a wrapper. The wrapper has a method
v() which returns the actual value. 

```
// The json string is updated by some thread.
private String jsonStr;
private Supplier<String> jsonSupplier = () -> jsonStr;

// ...

// create an instance with a supplier instead of string.
Konfiguration konfig = new JsonKonfiguration(jsonSupplier);
Konfig<Boolean> konfigValue = konfig.bool("some.konfig.key.deeply.nested");

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
