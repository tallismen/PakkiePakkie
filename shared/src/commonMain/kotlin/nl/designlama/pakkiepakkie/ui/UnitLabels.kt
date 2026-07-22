package nl.designlama.pakkiepakkie.ui

import androidx.compose.runtime.Composable
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.domain.units.WeightUnit
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.label_power
import pakkiepakkie.shared.generated.resources.label_power_hp
import pakkiepakkie.shared.generated.resources.label_power_kw
import pakkiepakkie.shared.generated.resources.label_power_pakkies
import pakkiepakkie.shared.generated.resources.label_weight_ledig_kg
import pakkiepakkie.shared.generated.resources.label_weight_ledig_lbs
import pakkiepakkie.shared.generated.resources.label_weight_ledig_slippers
import pakkiepakkie.shared.generated.resources.label_weight_rijklaar_kg
import pakkiepakkie.shared.generated.resources.label_weight_rijklaar_lbs
import pakkiepakkie.shared.generated.resources.label_weight_rijklaar_slippers
import pakkiepakkie.shared.generated.resources.unit_power_hp
import pakkiepakkie.shared.generated.resources.unit_power_kw
import pakkiepakkie.shared.generated.resources.unit_power_pakkies
import pakkiepakkie.shared.generated.resources.unit_preset_imperial
import pakkiepakkie.shared.generated.resources.unit_preset_metric
import pakkiepakkie.shared.generated.resources.unit_preset_pakkiepakkie
import pakkiepakkie.shared.generated.resources.unit_preset_standaard
import pakkiepakkie.shared.generated.resources.unit_weight_kg
import pakkiepakkie.shared.generated.resources.unit_weight_lbs
import pakkiepakkie.shared.generated.resources.unit_weight_slippers

@Composable
fun unitPresetLabel(preset: UnitPreset): String = when (preset) {
    UnitPreset.Standaard -> stringResource(Res.string.unit_preset_standaard)
    UnitPreset.Metric -> stringResource(Res.string.unit_preset_metric)
    UnitPreset.Imperial -> stringResource(Res.string.unit_preset_imperial)
    UnitPreset.PakkiePakkie -> stringResource(Res.string.unit_preset_pakkiepakkie)
}

@Composable
fun powerUnitLabel(unit: PowerUnit): String = when (unit) {
    PowerUnit.Kw -> stringResource(Res.string.unit_power_kw)
    PowerUnit.Hp -> stringResource(Res.string.unit_power_hp)
    PowerUnit.Pakkies -> stringResource(Res.string.unit_power_pakkies)
}

@Composable
fun weightUnitLabel(unit: WeightUnit): String = when (unit) {
    WeightUnit.Kg -> stringResource(Res.string.unit_weight_kg)
    WeightUnit.Lbs -> stringResource(Res.string.unit_weight_lbs)
    WeightUnit.Slippers -> stringResource(Res.string.unit_weight_slippers)
}

@Composable
fun powerSpecLabel(preferences: UnitPreferences): String = when (preferences.powerUnit) {
    PowerUnit.Kw -> if (preferences.preset == UnitPreset.Standaard) {
        stringResource(Res.string.label_power)
    } else {
        stringResource(Res.string.label_power_kw)
    }
    PowerUnit.Hp -> stringResource(Res.string.label_power_hp)
    PowerUnit.Pakkies -> stringResource(Res.string.label_power_pakkies)
}

@Composable
fun weightSpecLabel(preferences: UnitPreferences, rijklaar: Boolean): String = when (preferences.weightUnit) {
    WeightUnit.Kg -> if (rijklaar) {
        stringResource(Res.string.label_weight_rijklaar_kg)
    } else {
        stringResource(Res.string.label_weight_ledig_kg)
    }
    WeightUnit.Lbs -> if (rijklaar) {
        stringResource(Res.string.label_weight_rijklaar_lbs)
    } else {
        stringResource(Res.string.label_weight_ledig_lbs)
    }
    WeightUnit.Slippers -> if (rijklaar) {
        stringResource(Res.string.label_weight_rijklaar_slippers)
    } else {
        stringResource(Res.string.label_weight_ledig_slippers)
    }
}
