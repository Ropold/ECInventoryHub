import {translatedInfo} from "./utils/TranslatedInfo.ts";
import "./styles/Popup.css";

type NoPermissionPopupProps = {
    language: string;
    onClose: () => void;
}

export default function NoPermissionPopup(props: Readonly<NoPermissionPopupProps>) {
    return (
        <div className="popup-overlay">
            <div className="popup-content">
                <h3>{translatedInfo["No Permission"][props.language]}</h3>
                <p>{translatedInfo["Viewer no permission message"][props.language]}</p>
                <div className="popup-actions">
                    <button onClick={props.onClose} className="popup-cancel">{translatedInfo["Close"][props.language]}</button>
                </div>
            </div>
        </div>
    )
}
