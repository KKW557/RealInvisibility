# RealInvisibility

A Paper plugin designed for improving vanilla invisibility effect in Minecraft.

Lightweight, only modifying outgoing packets.

### Dependency

* [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)


### Compilation

To build RealInvisibility, you need JDK 21 or higher installed on your system.

* On Linux or macOS: `./gradlew build`
* Windows: `gradlew build`

you can then find builds of BridgingAnalyzer-Reborn in the `build/libs/` directory.


### Configuration

```yml
# Hide helmet?
helmet: true
# Hide chestplate?
chestplate: true
# Hide leggings?
leggings: true
# Hide boots?
boots: true
# Hide items in mainhand?
mainhand: true
# Hide items in offhand?
offhand: true
# Hide arrows in body?
arrows: true
```


### License

This project is licensed under the GPL GPL-3.0 license - see the [LICENSE](LICENSE) file for details.