import {useState} from "react";
import {createPortal} from "react-dom";
import {translatedInfo} from "./utils/TranslatedInfo.ts";
import "./styles/Popup.css";

type CardWarningProps = {
    language: string;
    title: string;
    items: string[];
}

// ⚠️ oben rechts auf einer Karte; Klick (oder Enter/Leertaste) öffnet ein Popup mit Details.
// Die Karten sind selbst Buttons – Klicks werden abgefangen, damit die Karte nicht öffnet.
export default function CardWarning(props: Readonly<CardWarningProps>) {
    const [showPopup, setShowPopup] = useState<boolean>(false);

    function handleClick(e: React.MouseEvent) {
        e.stopPropagation();
        setShowPopup(true);
    }

    function handleKeyDown(e: React.KeyboardEvent) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            e.stopPropagation();
            setShowPopup(true);
        }
    }

    return (
        <>
            <span
                className="card-warning"
                role="button"
                tabIndex={0}
                aria-label={props.title}
                title={props.title}
                onClick={handleClick}
                onKeyDown={handleKeyDown}
            >
                ⚠️
            </span>
            {/* Portal in document.body: Die Karte skaliert beim Hover (transform), das würde
                position: fixed des Overlays sonst einschränken. React-Events laufen trotz Portal
                zur Karte hoch, daher stopPropagation. */}
            {showPopup && createPortal(
                <div className="popup-overlay" onClick={(e) => e.stopPropagation()}>
                    <div className="popup-content">
                        <h3>{props.title}</h3>
                        {props.items.length > 0 && (
                            <ul className="popup-error-list">
                                {props.items.map((item) => (
                                    <li key={item}>{item}</li>
                                ))}
                            </ul>
                        )}
                        <div className="popup-actions">
                            <button onClick={() => setShowPopup(false)} className="popup-cancel">
                                {translatedInfo["Close"][props.language]}
                            </button>
                        </div>
                    </div>
                </div>,
                document.body
            )}
        </>
    )
}
