package dev.jbaby.wicket.oat;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SeasonalThemeStoreTest {

    private WicketTester tester;

    @AfterEach
    void tearDown() {
        if (tester != null) {
            tester.destroy();
        }
    }

    @Test
    void halloweenIsOnlyTheThirtyFirstOfOctober() {
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 10, 30))).isNull();
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 10, 31))).isEqualTo(OatTheme.HALLOWEEN);
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 11, 1))).isNull();
    }

    @Test
    void christmasRunsFromTheTenthOfDecemberToNewYearsEve() {
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 12, 9))).isNull();
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 12, 10))).isEqualTo(OatTheme.XMAS);
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 12, 24))).isEqualTo(OatTheme.XMAS);
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2026, 12, 31))).isEqualTo(OatTheme.XMAS);
        assertThat(SeasonalThemeStore.seasonalTheme(LocalDate.of(2027, 1, 1))).isNull();
    }

    @Test
    void theSeasonalThemeIsTheDefaultUntilTheUserChooses() {
        startOn(LocalDate.of(2026, 10, 31));
        assertThat(OatTheme.current()).isEqualTo(OatTheme.HALLOWEEN);

        OatTheme.setCurrent(OatTheme.NORD);
        assertThat(OatTheme.current()).isEqualTo(OatTheme.NORD);

        OatTheme.setCurrent(null);
        assertThat(OatTheme.current()).isEqualTo(OatTheme.HALLOWEEN);
    }

    @Test
    void outsideTheSeasonsTheConfiguredDefaultApplies() {
        startOn(LocalDate.of(2026, 6, 15));
        assertThat(OatTheme.current()).isEqualTo(OatTheme.BUSINESS);
    }

    private void startOn(LocalDate day) {
        ZoneId zone = ZoneId.of("Europe/Vienna");
        tester = new WicketTester();
        OatSettings.get(tester.getApplication())
                .setDefaultTheme(OatTheme.BUSINESS)
                .setThemeStore(new SeasonalThemeStore(Clock.fixed(day.atTime(12, 0).atZone(zone).toInstant(), zone)));
    }
}
