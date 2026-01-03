package net.hexuscraft.core.blockbuilder;

import java.util.List;

public enum CharacterBlock {

    A(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(3, 0),

            new Vector2(0, 1),
            new Vector2(3, 1),

            new Vector2(0, 2),
            new Vector2(1, 2),
            new Vector2(2, 2),
            new Vector2(3, 2),

            new Vector2(0, 3),
            new Vector2(3, 3),

            new Vector2(0, 4),
            new Vector2(3, 4)
    )),

    B(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),

            new Vector2(0, 1),
            new Vector2(3, 1),

            new Vector2(0, 2),
            new Vector2(1, 2),
            new Vector2(2, 2),

            new Vector2(0, 3),
            new Vector2(3, 3),

            new Vector2(0, 4),
            new Vector2(1, 4),
            new Vector2(2, 4)
    )),

    C(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(3, 0),

            new Vector2(0, 1),
            new Vector2(3, 1),

            new Vector2(0, 2),

            new Vector2(0, 3),
            new Vector2(3, 3),

            new Vector2(0, 4),
            new Vector2(1, 4),
            new Vector2(2, 4),
            new Vector2(3, 4)
    )),

    D(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),

            new Vector2(0, 1),
            new Vector2(3, 1),

            new Vector2(0, 2),
            new Vector2(3, 2),

            new Vector2(0, 3),
            new Vector2(3, 3),

            new Vector2(0, 4),
            new Vector2(1, 4),
            new Vector2(2, 4)
    )),

    E(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(3, 0),

            new Vector2(0, 1),

            new Vector2(0, 2),
            new Vector2(1, 2),
            new Vector2(2, 2),

            new Vector2(0, 3),

            new Vector2(0, 4),
            new Vector2(1, 4),
            new Vector2(2, 4),
            new Vector2(3, 4)
    )),

    F(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(3, 0),

            new Vector2(0, 1),

            new Vector2(0, 2),
            new Vector2(1, 2),

            new Vector2(0, 3),

            new Vector2(0, 4)
    )),

    G(List.of(
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(3, 0),

            new Vector2(0, 1),

            new Vector2(0, 2),
            new Vector2(2, 2),
            new Vector2(3, 2),

            new Vector2(0, 3),
            new Vector2(3, 3),

            new Vector2(0, 4),
            new Vector2(1, 4),
            new Vector2(2, 4),
            new Vector2(3, 4)
    ));

    public final List<Vector2> _blocks;

    CharacterBlock(List<Vector2> blocks) {
        _blocks = blocks;
    }

}
