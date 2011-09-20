package me.hwei.mmat;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum of all material ids accepted by the official server + client
 */
public enum Material {
    AIR(0),
    STONE(1),
    GRASS(2),
    DIRT(3),
    COBBLESTONE(4),
    WOOD(5),
    SAPLING(6),
    BEDROCK(7),
    WATER(8),
    STATIONARY_WATER(9),
    LAVA(10),
    STATIONARY_LAVA(11),
    SAND(12),
    GRAVEL(13),
    GOLD_ORE(14),
    IRON_ORE(15),
    COAL_ORE(16),
    LOG(17),
    LEAVES(18),
    SPONGE(19),
    GLASS(20),
    LAPIS_ORE(21),
    LAPIS_BLOCK(22),
    DISPENSER(23),
    SANDSTONE(24),
    NOTE_BLOCK(25),
    BED_BLOCK(26),
    POWERED_RAIL(27),
    DETECTOR_RAIL(28),
    PISTON_STICKY_BASE(29),
    WEB(30),
    LONG_GRASS(31),
    DEAD_BUSH(32),
    PISTON_BASE(33),
    PISTON_EXTENSION(34),
    WOOL(35),
    PISTON_MOVING_PIECE(36),
    YELLOW_FLOWER(37),
    RED_ROSE(38),
    BROWN_MUSHROOM(39),
    RED_MUSHROOM(40),
    GOLD_BLOCK(41),
    IRON_BLOCK(42),
    DOUBLE_STEP(43),
    STEP(44),
    BRICK(45),
    TNT(46),
    BOOKSHELF(47),
    MOSSY_COBBLESTONE(48),
    OBSIDIAN(49),
    TORCH(50),
    FIRE(51),
    MOB_SPAWNER(52),
    WOOD_STAIRS(53),
    CHEST(54),
    REDSTONE_WIRE(55),
    DIAMOND_ORE(56),
    DIAMOND_BLOCK(57),
    WORKBENCH(58),
    CROPS(59),
    SOIL(60),
    FURNACE(61),
    BURNING_FURNACE(62),
    SIGN_POST(63, 1),
    WOODEN_DOOR(64),
    LADDER(65),
    RAILS(66),
    COBBLESTONE_STAIRS(67),
    WALL_SIGN(68, 1),
    LEVER(69),
    STONE_PLATE(70),
    IRON_DOOR_BLOCK(71),
    WOOD_PLATE(72),
    REDSTONE_ORE(73),
    GLOWING_REDSTONE_ORE(74),
    REDSTONE_TORCH_OFF(75),
    REDSTONE_TORCH_ON(76),
    STONE_BUTTON(77),
    SNOW(78),
    ICE(79),
    SNOW_BLOCK(80),
    CACTUS(81),
    CLAY(82),
    SUGAR_CANE_BLOCK(83),
    JUKEBOX(84),
    FENCE(85),
    PUMPKIN(86),
    NETHERRACK(87),
    SOUL_SAND(88),
    GLOWSTONE(89),
    PORTAL(90),
    JACK_O_LANTERN(91),
    CAKE_BLOCK(92, 1),
    DIODE_BLOCK_OFF(93),
    DIODE_BLOCK_ON(94),
    LOCKED_CHEST(95),
    TRAP_DOOR(96),
    MONSTER_EGGS(97),
    SMOOTH_BRICK(98),
    HUGE_MUSHROOM_1(99),
    HUGE_MUSHROOM_2(100),
    IRON_FENCE(101),
    THIN_GLASS(102),
    MELON_BLOCK(103),
    PUMPKIN_STEM(104),
    MELON_STEM(105),
    VINE(106),
    FENCE_GATE(107),
    BRICK_STAIRS(108),
    SMOOTH_STAIRS(109),
    // ----- Item Separator -----
    IRON_SPADE(256, 1, 250),
    IRON_PICKAXE(257, 1, 250),
    IRON_AXE(258, 1, 250),
    FLINT_AND_STEEL(259, 1, 64),
    APPLE(260),
    BOW(261, 1),
    ARROW(262),
    COAL(263),
    DIAMOND(264),
    IRON_INGOT(265),
    GOLD_INGOT(266),
    IRON_SWORD(267, 1, 250),
    WOOD_SWORD(268, 1, 59),
    WOOD_SPADE(269, 1, 59),
    WOOD_PICKAXE(270, 1, 59),
    WOOD_AXE(271, 1, 59),
    STONE_SWORD(272, 1, 131),
    STONE_SPADE(273, 1, 131),
    STONE_PICKAXE(274, 1, 131),
    STONE_AXE(275, 1, 131),
    DIAMOND_SWORD(276, 1, 1561),
    DIAMOND_SPADE(277, 1, 1561),
    DIAMOND_PICKAXE(278, 1, 1561),
    DIAMOND_AXE(279, 1, 1561),
    STICK(280),
    BOWL(281),
    MUSHROOM_SOUP(282, 1),
    GOLD_SWORD(283, 1, 32),
    GOLD_SPADE(284, 1, 32),
    GOLD_PICKAXE(285, 1, 32),
    GOLD_AXE(286, 1, 32),
    STRING(287),
    FEATHER(288),
    SULPHUR(289),
    WOOD_HOE(290, 1, 59),
    STONE_HOE(291, 1, 131),
    IRON_HOE(292, 1, 250),
    DIAMOND_HOE(293, 1, 1561),
    GOLD_HOE(294, 1, 32),
    SEEDS(295),
    WHEAT(296),
    BREAD(297),
    LEATHER_HELMET(298, 1, 33),
    LEATHER_CHESTPLATE(299, 1, 47),
    LEATHER_LEGGINGS(300, 1, 45),
    LEATHER_BOOTS(301, 1, 39),
    CHAINMAIL_HELMET(302, 1, 66),
    CHAINMAIL_CHESTPLATE(303, 1, 95),
    CHAINMAIL_LEGGINGS(304, 1, 91),
    CHAINMAIL_BOOTS(305, 1, 78),
    IRON_HELMET(306, 1, 135),
    IRON_CHESTPLATE(307, 1, 191),
    IRON_LEGGINGS(308, 1, 183),
    IRON_BOOTS(309, 1, 159),
    DIAMOND_HELMET(310, 1, 271),
    DIAMOND_CHESTPLATE(311, 1, 383),
    DIAMOND_LEGGINGS(312, 1, 367),
    DIAMOND_BOOTS(313, 1, 319),
    GOLD_HELMET(314, 1, 67),
    GOLD_CHESTPLATE(315, 1, 95),
    GOLD_LEGGINGS(316, 1, 91),
    GOLD_BOOTS(317, 1, 79),
    FLINT(318),
    PORK(319),
    GRILLED_PORK(320),
    PAINTING(321),
    GOLDEN_APPLE(322),
    SIGN(323, 1),
    WOOD_DOOR(324, 1),
    BUCKET(325, 1),
    WATER_BUCKET(326, 1),
    LAVA_BUCKET(327, 1),
    MINECART(328, 1),
    SADDLE(329, 1),
    IRON_DOOR(330, 1),
    REDSTONE(331),
    SNOW_BALL(332, 16),
    BOAT(333, 1),
    LEATHER(334),
    MILK_BUCKET(335, 1),
    CLAY_BRICK(336),
    CLAY_BALL(337),
    SUGAR_CANE(338),
    PAPER(339),
    BOOK(340),
    SLIME_BALL(341),
    STORAGE_MINECART(342, 1),
    POWERED_MINECART(343, 1),
    EGG(344, 16),
    COMPASS(345),
    FISHING_ROD(346, 1, 64),
    WATCH(347),
    GLOWSTONE_DUST(348),
    RAW_FISH(349),
    COOKED_FISH(350),
    INK_SACK(351),
    BONE(352),
    SUGAR(353),
    CAKE(354, 1),
    BED(355, 1),
    DIODE(356),
    COOKIE(357),
    MAP(358, 1),
    SHEARS(359, 1, 238),
    MELON(360),
    PUMPKIN_SEEDS(361),
    MELON_SEEDS(362),
    RAW_BEEF(363),
    COOKED_BEEF(364),
    RAW_CHICKEN(365),
    COOKED_CHICKEN(366),
    ROTTEN_FLESH(367),
    ENDER_PEARL(368),
    GOLD_RECORD(2256, 1),
    GREEN_RECORD(2257, 1);

    private final int id;
    private static final Map<Integer, Material> lookupId = new HashMap<Integer, Material>();
    private static final Map<String, Material> lookupName = new HashMap<String, Material>();
    private final int maxStack;
    private final short durability;

    private Material(final int id) {
        this(id, 64);
    }

    private Material(final int id, final int stack) {
        this(id, stack, -1);
    }

    
    private Material(final int id, final int stack, final int durability) {
        this.id = id;
        this.durability = (short) durability;
        this.maxStack = stack;
    }


    /**
     * Gets the item ID or block ID of this Material
     *
     * @return ID of this material
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the maximum amount of this material that can be held in a stack
     *
     * @return Maximum stack size for this material
     */
    public int getMaxStackSize() {
        return maxStack;
    }

    /**
     * Gets the maximum durability of this material
     *
     * @return Maximum durability for this material
     */
    public short getMaxDurability() {
        return durability;
    }


    /**
     * Checks if this Material is a placable block
     *
     * @return true if this material is a block
     */
    public boolean isBlock() {
        return id < 256;
    }

    /**
     * Attempts to get the Material with the given ID
     *
     * @param id ID of the material to get
     * @return Material if found, or null
     */
    public static Material getMaterial(final int id) {
        return lookupId.get(id);
    }

    /**
     * Attempts to get the Material with the given name.
     * This is a normal lookup, names must be the precise name they are given
     * in the enum.
     *
     * @param name Name of the material to get
     * @return Material if found, or null
     */
    public static Material getMaterial(final String name) {
        return lookupName.get(name);
    }

    /**
     * Attempts to match the Material with the given name.
     * This is a match lookup; names will be converted to uppercase, then stripped
     * of special characters in an attempt to format it like the enum
     *
     * @param name Name of the material to get
     * @return Material if found, or null
     */
    public static Material matchMaterial(final String name) {
        Material result = null;

        try {
            result = getMaterial(Integer.parseInt(name));
        } catch (NumberFormatException ex) {}

        if (result == null) {
            String filtered = name.toUpperCase();

            filtered = filtered.replaceAll("\\s+", "_").replaceAll("\\W", "");
            result = lookupName.get(filtered);
        }

        return result;
    }

    static {
        for (Material material : values()) {
            lookupId.put(material.getId(), material);
            lookupName.put(material.name(), material);
        }
    }
}

