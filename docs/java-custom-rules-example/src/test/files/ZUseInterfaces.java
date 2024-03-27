class Main {
    void use() {
        Impl var = new Impl(); // Noncompliant {{Declare "var" as "Api" instead of "Impl"}}
        var.init();
    }
}

class Impl implements Api {
    @Override
    public void init() {
    }
}

interface Api {
    void init();
}

// do not report bogus error

class Bogus {
    void method() {
        String str;
        str.toString();
        Integer i;
        i.toString();
        Impl unused;
        StringBuffer sb;
        sb.toString();
    }
}

interface IBogus {
    void method(Impl arg);
}