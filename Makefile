SRC_DIR=IliasBuddy
GRADLE_OPTIONS=--stacktrace

BUILD_DIR=$(SRC_DIR)/app/build
LOCATION_RELEASE_APK=$(BUILD_DIR)/outputs/apk/release/app-release.apk
LOCATION_DEBUG_APK=$(BUILD_DIR)/outputs/apk/debug/app-debug.apk

VERSION_NAME=$(shell sed -n 's/versionName "\(.*\)"/\1/p' IliasBuddy/app/build.gradle | sed -e 's/\s\+//g')
VERSION_NAME_CODE=$(shell echo $(VERSION_NAME) | sed -e 's/\.\+//g')

OUT_RELEASE_APK=IliasBuddy_v$(VERSION_NAME_CODE).apk
OUT_DEBUG_APK=IliasBuddyDebug_v$(VERSION_NAME_CODE).apk

.PHONY: clean test build_debug build_release

all: build_release

clean:
	rm -f $(OUT_DEBUG_APK) $(OUT_RELEASE_APK)
	rm -rf $(BUILD_DIR)

test:
	cd $(SRC_DIR); \
	./gradlew test $(GRADLE_OPTIONS)

build_debug:
	cd $(SRC_DIR); \
	./gradlew assembleDebug $(GRADLE_OPTIONS)
	cp $(LOCATION_DEBUG_APK) $(OUT_DEBUG_APK)

build_release:
	@echo "----------------------------------------------------------------------------"
	@echo "You need to set the following environment variables to sign the APK:"
	@echo "KEY_STORE_PATH=$(KEY_STORE_PATH)"
	@echo "KEY_STORE_PASSWORD=$(KEY_STORE_PASSWORD)"
	@echo "KEY_ALIAS=$(KEY_ALIAS)"
	@echo "KEY_ALIAS_PASSWORD=$(KEY_ALIAS_PASSWORD)"
	@echo "----------------------------------------------------------------------------"
	cd $(SRC_DIR); \
	./gradlew assembleRelease $(GRADLE_OPTIONS)
	cp $(LOCATION_RELEASE_APK) $(OUT_RELEASE_APK)
