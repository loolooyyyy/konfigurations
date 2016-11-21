
## Java Configuration Library

Similar to apache commons configuration but strives to be simple and have as
little exteranl dependencies as possible.

Example usage:

```java

// Create as many sources as necessary
JsonKonfigSource js = new JsonKonfigSource(this::readJsonString);
InMemoryKonfigSource ms = new InMemoryKonfigSource(this::readMapOfConfigs);

// Kombine them (ms takes priority over js).
Konfiguration konfig = new KonfigurationKombiner(ms, js);

// Get the value, notice the .v()
boolean b = konfig.bool   ("some.konfig.key.deeply.nested").v()
int     i = konfig.int_   ("some.int").v()
long    l = konfig.long_  ("some.long").v()
String  s = konfig.string ("aString").v()
Double  d = konfig.double ("double").v()

List<String>         list = konfig.list("a.nice.string.list", String.class).v()
Map<String, Integer> map  = konfig.map ("my.map", int.class).v()

```

### Overrides

Konfiguration sources can override each other. The first source which contains
the requested key is selected.


### Live updates

Value can be updated during runtime. A konfiguration instance does not return 
a value directly but returns a wrapper. The wrapper has a method v() which 
returns the actual value.  

```java

public class KonfigDemo {

    static String theJsonKonfigString = "{ \"isAllowed\": true }"; 

    static Konfiguration konfig = 
        new KonfigurationKombiner(new JsonKonfigSource(() -> theJsonKonfigString));;

    public static void main(final String[] args) {

        KonfigV<Boolean> amIAllowed = konfig.bool("isAllowed");

        // Get notified when the key <something> changes.
        amIAllowed.register(updatedKey -> {
            System.out.println("Hey! we're updated:: " + konfig.bool(updatedKey)));        
            System.out.println("Also accessible from: " + amIAllowed.v());        
        });

        assert amIAllowed.v();

        theJsonKonfigString = "{ \"isAllowed\": false }";
        konfig.update(); // Must be called!

        // By now, the System.out.println(...) thing we wrote above is also called.
        assert !amIAllowed.v();
    }
}

```

### Default values

If you are okay with non-existing values, you may get a defaulted-konfiguration-value:

```java

// Use stringD instead of usual string (same for int_, intD, long_, longD, ...)
KonfigV<String> defValue = konfig.stringD("non.exisintg.key_hahaha_:)))_boo");

// Assuming the crazy key above does not exist in the konfiguration,
// Instead of plain v():
String value = defValue.v("somethingDefault");

assert "somethingDefault".equals(value);

```

### Registering to recieve update events

It's possible to register on a specific key (the method register() on the `V`
interface) or to any configuration change (on the Konfiguration interface).

The snippet above registers itself on `V` interface.

De-Registering is possible but not necessary as no hard reference is kept to
listeners (GC works as expected).

Registering to multiple keys from a single listener is possible.


### Lists and Maps and Custom Types

Getting a list or map  or a custom objectis possible, as long as the 
underlying source can parse it from the actual configuration. The 
JsonKonfigSource uses Jackson for parsing json, so if the Jackson parser can 
parse the map / list / object, so can the configuration.

### JsonKonfigSource notes

a String can span multiple lines if it's declared in an array

```json
{
    "someLongStr": [
        "I'm ",
        "very ",
        "multi ",
        "line."
    ]
}
```

```java
JsonKonfigSource js = new JsonKonfiguration(...); 
Konfiguration konfig = new KonfigurationKombiner(js);
assert "I'm very multi line." == konfig.string("someLongStr");
```


### Assumptions / Limitaions:
 
 - Currently, custom types and ALL the required fields corresponding to those 
   read from json string, MUST be public. (jackson wont find private, protected
   and package local fields AND classes (important: both class and fields, must
   be public)). This affects list(), map() and custom() methods.

 - Changing a key type (such as from int to String) during an update is not recomended.
   TODO: disallow this.

 - Removing a key or adding a new one during IS supported.
   TODO: disallow this.

