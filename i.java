import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("unchecked")
public final class i { public static void main(String... a) { new i().act(a); }
private static long l = 0L;
public static void o(Object q, Object... qq) { if(l==0) System.out.print("0 "); System.out.println(q); for(final Object x: qq) System.out.print(x); }
public static void n(Object q, Object... qq) { System.out.println(q); for(final Object x: qq) o(x); o("\n", l++, ' '); }

public void act(String... a) {

  List<Map<String, Set<Integer>>> l = new ArrayList<>(){};
  List<Map<String, Set<Integer>>> m = new ArrayList<>(){};

        final Type[] t0 = ((ParameterizedType) m
              .getClass()
              .getGenericSuperclass())
              .getActualTypeArguments();

        final Type[] t1 = ((ParameterizedType) l
              .getClass()
              .getGenericSuperclass())
              .getActualTypeArguments();

          n(t0);
          n(t1);

          n(' ');
          n(' ');
          n(' ');

          for(Type t: t0)
          checkIsClassOrParametrizedType(t, null);
}


  private static void checkIsClassOrParametrizedType(final Type p,
                                                     Type root) {
      n("p is: ", p);
      if (root == null)
          root = p;
      if (p == null)
          return;

      if (!(p instanceof ParameterizedType))
          return;
      final ParameterizedType pp = (ParameterizedType) p;

      checkIsClassOrParametrizedType(root, pp.getRawType());
      for (final Type ppp : pp.getActualTypeArguments())
          checkIsClassOrParametrizedType(root, ppp);
  }



}
