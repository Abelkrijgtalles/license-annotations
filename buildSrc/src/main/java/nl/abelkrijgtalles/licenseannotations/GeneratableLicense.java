package nl.abelkrijgtalles.licenseannotations;

import java.util.List;

public class GeneratableLicense {

    private final String name;
    private final String packageName;
    private final String rawId;
    private final List<String> alternativeNames;

    public GeneratableLicense(String name, String packageName, String rawId, List<String> alternativeNames) {

        this.name = name;
        this.packageName = packageName;
        this.rawId = rawId;
        this.alternativeNames = alternativeNames;
    }

    public List<String> getAlternativeNames() {

        return alternativeNames;
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
