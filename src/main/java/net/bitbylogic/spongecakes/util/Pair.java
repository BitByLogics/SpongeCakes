package net.bitbylogic.spongecakes.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Pair<K, V> {

    private K key;
    private V value;

}
