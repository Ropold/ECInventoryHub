import {translatedInfo} from "./utils/TranslatedInfo.ts";
import * as React from "react";
import {LanguagesImages, getLanguageName} from "./utils/FlagImages.ts";

type FooterProps = {
    language: string;
    setLanguage: React.Dispatch<React.SetStateAction<string>>
};

export default function Footer(props: Readonly<FooterProps>) {
    const [showLanguagePopup, setShowLanguagePopup] = React.useState(false);

    return (
        <footer className="footer">

            <div className="footer-container">
                <p>{translatedInfo["EC Inventory Hub 2026 by R.Stolz"][props.language]}</p>

                <div
                    className="clickable-header button-footer"
                    onClick={() => setShowLanguagePopup(true)}
                >
                    <h2 className="header-title">{getLanguageName(props.language)}</h2>
                    <img src={LanguagesImages[props.language]} alt="Language Logo" className="logo-image" />
                </div>
            </div>


            {showLanguagePopup && (
                <div
                    className="popup-overlay"
                    onClick={() => setShowLanguagePopup(false)}
                >
                    <div
                        className="popup-content"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h2>Select Language</h2>
                        <div className="popup-language-options">
                            {["en", "de", "pl", "es", "fr", "it", "ru"].map((lang) => (
                                <button
                                    key={lang}
                                    className="language-option-button"
                                    onClick={() => {
                                        props.setLanguage(lang);
                                        setShowLanguagePopup(false);
                                    }}
                                >
                                    <img
                                        src={LanguagesImages[lang]}
                                        alt={lang}
                                        className="language-flag"
                                    />
                                    {getLanguageName(lang)}
                                </button>
                            ))}
                        </div>
                        <button
                            className="popup-cancel margin-top-20"
                            onClick={() => setShowLanguagePopup(false)}
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            )}
        </footer>
    )
}