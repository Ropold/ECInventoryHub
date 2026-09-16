import {translatedInfo} from "./utils/TranslatedInfo.ts";
import * as React from "react";
import {LanguagesImages, getLanguageName} from "./utils/FlagImages.ts";
import axios from "axios";

type FooterProps = {
    language: string;
    setLanguage: React.Dispatch<React.SetStateAction<string>>
    user: string;
};

function setPreferredLanguage(languageIso: string) {
    axios.post(`/api/users/me/language/${languageIso}`)
        .then(() => {
            console.log("Language updated successfully");
        })
        .catch((error) => {
            console.error("Error updating language:", error);
        });
}

export default function Footer(props: Readonly<FooterProps>) {
    const [showLanguagePopup, setShowLanguagePopup] = React.useState(false);

    React.useEffect(() => {
        if (!showLanguagePopup) return;

        function handleEscape(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setShowLanguagePopup(false);
            }
        }

        document.addEventListener("keydown", handleEscape);
        return () => document.removeEventListener("keydown", handleEscape);
    }, [showLanguagePopup]);

    return (
        <footer className="footer">

            <div className="footer-container">
                <p>{translatedInfo["EC Inventory Hub 2026 by R.Stolz"][props.language]}</p>

                <button
                    type="button"
                    className="clickable-header button-footer"
                    onClick={() => setShowLanguagePopup(true)}
                >
                    <h2 className="header-title">{getLanguageName(props.language)}</h2>
                    <img src={LanguagesImages[props.language]} alt="Language Logo" className="logo-image" />
                </button>
            </div>


            {showLanguagePopup && (
                <div
                    className="popup-overlay"
                    role="presentation"
                    onClick={(e) => {
                        // nur schliessen, wenn wirklich der Hintergrund getroffen wurde
                        if (e.target === e.currentTarget) {
                            setShowLanguagePopup(false);
                        }
                    }}
                >
                    <div
                        className="popup-content"
                        role="dialog"
                        aria-modal="true"
                        aria-label="Select Language"
                    >
                        <h2>Select Language</h2>
                        <div className="popup-language-options">
                            {["en", "de", "pl", "es", "fr", "it", "pt", "ru", "tr"].map((lang) => (
                                <button
                                    key={lang}
                                    className="language-option-button"
                                    onClick={() => {
                                        props.setLanguage(lang);
                                        if (props.user !== "anonymousUser") {
                                            setPreferredLanguage(lang);
                                        }
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