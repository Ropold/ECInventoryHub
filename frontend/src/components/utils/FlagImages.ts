import dePic from "../../assets/flags/de.svg";
import gbPic from "../../assets/flags/gb.svg";
import plPic from "../../assets/flags/pl.svg";
import esPic from "../../assets/flags/es.svg";
import frPic from "../../assets/flags/fr.svg";
import itPic from "../../assets/flags/it.svg";
import ptPic from "../../assets/flags/pt.svg";
import ruPic from "../../assets/flags/ru.svg";
import trPic from "../../assets/flags/tr.svg";

export const flagImages: Record<string, string> = {
    de: dePic,
    gb: gbPic,
    pl: plPic,
    es: esPic,
    fr: frPic,
    it: itPic,
    pt: ptPic,
    ru: ruPic,
    tr: trPic,
}

export const LanguagesImages: Record<string, string> = {
    de: dePic,
    en: gbPic,
    pl: plPic,
    es: esPic,
    fr: frPic,
    it: itPic,
    pt: ptPic,
    ru: ruPic,
    tr: trPic,
};

export const countryNameToIsoCode: Record<string, string> = {
    "Germany": "de",
    "United Kingdom": "gb",
    "Poland": "pl",
    "Spain": "es",
    "France": "fr",
    "Italy": "it",
    "Portugal": "pt",
    "Russia": "ru",
    "Turkey": "tr",
}

export function getLanguageName(code: string): string {
    switch (code) {
        case "en": return "English";
        case "de": return "Deutsch";
        case "pl": return "Polski";
        case "es": return "Español";
        case "fr": return "Français";
        case "it": return "Italiano";
        case "pt": return "Português";
        case "ru": return "Русский";
        case "tr": return "Türkçe";
        default: return code;
    }
}