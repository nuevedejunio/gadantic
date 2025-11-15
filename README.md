# Gadantic

Genetic algorithm optimizer for Palia garden layouts using Jenetics.

## Overview

Gadantic uses evolutionary algorithms to find optimal 9x9 crop garden layouts from the video game Palia. The optimizer maximizes perk distribution efficiency across 15 crop types with different sizes and abilities.

## Problem Domain

**Garden Grid**: 9x9 plot (81 tiles)

**Crops** (15 types):
- 1x1: Tomatoes, Potatoes, Cabbage, Rice, Wheat, Corn, Carrots, Onions, Bok Choy, Cotton
- 2x2: Blueberries, Beans, Peppers, Pumpkins  
- 3x3: Apples

**Perks** (4 types):
- WATER: Tomatoes, Potatoes, Cabbage
- HARVEST: Rice, Wheat, Corn, Blueberries, Beans, Apples
- WEED: Carrots, Onions, Bok Choy
- QUALITY: Cotton, Peppers, Pumpkins

Each crop buffs adjacent tiles (non-diagonal) with its perk. Crops need perks applied ≥ their size to be fully buffed.

## Algorithm

**Genotype**: 81-gene integer chromosome (crop ordinals)

**Constraints**: Repairs invalid layouts (overlaps, out-of-bounds)

**Fitness**: Weighted sum of:
- Water/Weed/Quality/Harvest coverage (tiles fully buffed / 81)
- Unique crops (distinct types / 15)
- Buff efficiency (successful applications / available slots)

## Tech Stack

- Java 21
- Quarkus 3.23.0
- Jenetics 8.2.0 (genetic algorithm library)
- Maven

## Build & Run

```bash
./mvnw quarkus:dev
```

## Configuration

Fitness coefficients configurable via `application.properties`:
```properties
fitness.water-retention=1.0
fitness.weed-prevention=1.0
fitness.quality-boost=1.0
fitness.harvest-increase=1.0
fitness.unique-crops=1.0
fitness.buff-efficiency=1.0
fitness.horizontal-symmetry=0.0
fitness.vertical-symmetry=0.0
fitness.rotational-symmetry=0.0
```

See `config/list-of.application.properties` for all supported properties and defaults.
