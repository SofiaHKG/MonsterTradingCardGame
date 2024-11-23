package at.mtgc.application.packages.repository;

import at.mtgc.application.packages.entity.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageRepository {
    private final List<Package> packages;

    public PackageRepository() {
        this.packages = new ArrayList<>();
    }

    public void addPackage(Package pack) {
        packages.add(pack);
    }

    public Package getNextPackage() {
        if (packages.isEmpty()) {
            return null;
        }
        return packages.remove(0);
    }

    public boolean hasPackages() {
        return !packages.isEmpty();
    }
}
