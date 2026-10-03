import {createPortal} from "react-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/Popup.css";

type MissingDocumentsPopupProps = {
    language: string;
    missingDocuments: string[];
    onClose: () => void;
}

// Wird per Portal in document.body gerendert: Die AssignmentCard ist selbst ein Button und
// skaliert beim Hover (transform), das würde position: fixed des Overlays sonst einschränken.
// React-Events laufen trotz Portal zur Karte hoch, daher stopPropagation.
export default function MissingDocumentsPopup(props: Readonly<MissingDocumentsPopupProps>) {
    return createPortal(
        <div className="popup-overlay" onClick={(e) => e.stopPropagation()}>
            <div className="popup-content">
                <h3>{translatedInfo["Documents missing"][props.language]}</h3>
                <ul className="popup-error-list">
                    {props.missingDocuments.map((document) => (
                        <li key={document}>{document}</li>
                    ))}
                </ul>
                <div className="popup-actions">
                    <button onClick={props.onClose} className="popup-cancel">{translatedInfo["Close"][props.language]}</button>
                </div>
            </div>
        </div>,
        document.body
    )
}
