# Vitalis

[![Fabric](https://img.shields.io/badge/Fabric-26.2-d8b400)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Клиентский HUD-мод для Minecraft **26.2**: здоровье над мобами, подсказки по
зачарованиям, информация о цели, прочность брони, таймеры зелий и компактный
оверлей. Всё настраивается и всё можно выключить.

## Что умеет

- **Полоски здоровья** над игроками, враждебными, животными и остальными.
  Три режима: полоса, цифры, полоса + цифры (`health.displayMode`).
- **Помощник зачарований** — шансы и уровни прямо в столе зачарований,
  подсветка лучшего варианта.
- **Информация о цели** — имя, здоровье, тип, расстояние и эффекты существа,
  на которое смотришь.
- **Прочность брони** — процент и цвет по остатку (зелёный → жёлтый → красный).
- **Таймеры зелий** — длительность, уровень, иконка.
- **Компактный оверлей** — координаты, направление, FPS, уровень света, биом, часы.
  Позиция задаётся в конфиге.
- **Mod Menu** — экран настроек, если установлен Mod Menu.

## Установка

1. Установите **Fabric Loader** и **Fabric API** для Minecraft **26.2**.
2. Положите `vitalis-1.3.0.jar` в `.minecraft/mods/`.
3. (Опционально) установите **Mod Menu**, чтобы менять настройки в игре.

Мод клиентский: на сервер его ставить не нужно, античит он не трогает —
только читает данные, которые клиент и так получает.

Сборка (JDK 25):

```bash
./gradlew build     # Windows: gradlew.bat build
# build/libs/vitalis-1.3.0.jar
```

## Конфиг

Файл: `.minecraft/config/vitalis.json` (создаётся при первом запуске).

```json
{
  "health":  { "enabled": true, "showPlayers": true, "showHostile": true,
               "displayMode": 0, "maxDistance": 24, "yOffset": 0.35 },
  "enchant": { "enabled": true, "showChances": true, "highlightBest": true },
  "overlay": { "enabled": true, "showCoords": true, "showDirection": true,
               "showFps": false, "showLightLevel": true,
               "positionX": 4, "positionY": 4 },
  "target":  { "enabled": true, "showName": true, "showHealth": true,
               "showDistance": true, "showEffects": true, "maxDistance": 8 },
  "armor":   { "enabled": true, "showDurability": true,
               "colorByDurability": true },
  "potion":  { "enabled": true, "showDuration": true, "showIcon": true }
}
```

`displayMode`: `0` — полоса, `1` — цифры, `2` — полоса и цифры.

## Совместимость

| Компонент | Версия |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | для 26.2 |
| Java | 25 |
| Mod Menu | опционально |

## Структура

```
src/client/java/com/vitalis/
  VitalisClient.java              — точка входа, тик HUD
  config/VitalisConfig.java       — JSON-конфиг и значения по умолчанию
  config/VitalisConfigScreen.java — экран настроек
  hud/HealthDisplayRenderer.java  — здоровье над сущностями
  hud/TargetInfoRenderer.java     — карточка цели
  hud/ArmorDurabilityRenderer.java
  hud/PotionTimerRenderer.java
  hud/CompactHUDRenderer.java
  enchant/EnchantHelperRenderer.java
  mixin/                          — доступ к данным экрана зачарований и здоровья
```

## Лицензия

MIT — см. [LICENSE](LICENSE).
