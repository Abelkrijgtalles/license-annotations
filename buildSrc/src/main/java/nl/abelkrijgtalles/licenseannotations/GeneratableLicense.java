package nl.abelkrijgtalles.licenseannotations;

public class GeneratableLicense {

    private final String name;
    private final String packageName;
    private final String rawId;

    public GeneratableLicense(String name, String packageName, String rawId) {

        this.name = name;
        this.packageName = packageName;
        this.rawId = rawId;
    }

    public String getRawId() {

        return rawId;
    }

    public String getPackage() {

        return packageName;
    }

    public String getName() {

        return name;
    }

}
