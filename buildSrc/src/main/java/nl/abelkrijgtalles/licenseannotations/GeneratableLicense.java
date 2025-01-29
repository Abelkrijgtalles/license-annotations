package nl.abelkrijgtalles.licenseannotations;

public class GeneratableLicense {

    private final String name;
    private final String packageName;

    public GeneratableLicense(String name, String packageName) {

        this.name = name;
        this.packageName = packageName;
    }

    public String getPackage() {

        return packageName;
    }

    public String getName() {

        return name;
    }

}
