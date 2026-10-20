package com.littlepay.assessment.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PanMaskerTest {

    @Test
    void keepsFirstSixAndLastFourDigits() {
        assertThat(PanMasker.mask("5500005555555559")).isEqualTo("550000******5559");
        assertThat(PanMasker.mask("4111111111111111")).isEqualTo("411111******1111");
        assertThat(PanMasker.mask("1234567890123450123")).isEqualTo("123456*********0123");
    }

    @Test
    void shortValuesOnlyShowLastFourDigits() {
        assertThat(PanMasker.mask("1234567890")).isEqualTo("******7890");
        assertThat(PanMasker.mask("123")).isEqualTo("123");
    }

    @Test
    void nullAndEmptyPassThrough() {
        assertThat(PanMasker.mask(null)).isNull();
        assertThat(PanMasker.mask("")).isEqualTo("");
    }
}
