val name = providers.gradleProperty("name")
rootProject.name = name.get()
include(":commons-exec-patched")