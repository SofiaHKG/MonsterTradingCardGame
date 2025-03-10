package at.mtgc.application.packages.service;

import at.mtgc.application.packages.entity.Package;
import at.mtgc.application.packages.repository.PackageRepository;

public class PackageService {
    private final PackageRepository packageRepository;

    public PackageService(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public void addPackage(Package pack) {
        packageRepository.addPackage(pack);
    }

    public Package getNextPackage() {
        return packageRepository.getNextPackage();
    }

    public boolean hasPackages() {
        return packageRepository.hasPackages();
    }

    public boolean acquirePackage(String username) {
        boolean success = packageRepository.acquirePackage(username);
        System.out.println("Acquire package result for " + username + ": " + success); // Debug
        return success;
    }

}
