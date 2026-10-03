import type {AssignmentModel} from "../models/AssignmentModel.ts";
import type {DeviceType} from "../models/DeviceModel.ts";
import {useNavigate} from "react-router-dom";
import {useState} from "react";
import MissingDocumentsPopup from "./MissingDocumentsPopup.tsx";
import {formatDate, getDeviceLabel} from "../utils/ComponentsFunctions.tsx";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/assignment/AssignmentCard.css";

type AssignmentCardProps = {
    assignment: AssignmentModel;
    language: string;
    hideEmployee?: boolean;
    hideDevice?: boolean;
}

const deviceTypeIcons: Record<DeviceType, string> = {
    LAPTOP: "💻",
    PHONE: "📱",
    TABLET: "📱",
    MONITOR: "🖥️",
    ACCESSORY: "🖱️",
    OTHER: "📦",
};

export default function AssignmentCard(props: Readonly<AssignmentCardProps>){
    const navigate = useNavigate();
    const [showDocumentsPopup, setShowDocumentsPopup] = useState<boolean>(false);

    const handleCardClick = () => {
        navigate(`/assignments/${props.assignment.id}`);
    }

    const {device, employee, assignedDate, returnedDate} = props.assignment;
    const isActive = !returnedDate;
    const deviceLabel = `${deviceTypeIcons[device.type]} ${getDeviceLabel(device)}`;
    const deviceName = [device.manufacturer, device.modelName].filter(Boolean).join(" ");
    // Nur bei aktiven Zuweisungen warnen; das Popup listet auf, welche Kopie noch fehlt
    const missingDocuments = [
        !props.assignment.copyHandedToEmployee && translatedInfo["Copy Handed To Employee"][props.language],
        !props.assignment.copyFiledInPersonnelFile && translatedInfo["Copy Filed In Personnel File"][props.language],
    ].filter((document): document is string => Boolean(document));
    const showDocumentsWarning = isActive && missingDocuments.length > 0;

    // Das ⚠️ liegt innerhalb der Karte (selbst ein Button) – Klick/Taste darf die Karte nicht öffnen
    function handleWarningClick(e: React.MouseEvent) {
        e.stopPropagation();
        setShowDocumentsPopup(true);
    }

    function handleWarningKeyDown(e: React.KeyboardEvent) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            e.stopPropagation();
            setShowDocumentsPopup(true);
        }
    }
    const dateText = isActive
        ? formatDate(assignedDate)
        : `${formatDate(assignedDate)} – ${formatDate(returnedDate ?? undefined)}`;

    return (
        <button
            type="button"
            className={`assignment-card${isActive ? "" : " assignment-card-returned"}`}
            onClick={handleCardClick}
        >
            {showDocumentsWarning && (
                <span
                    className="assignment-card-warning"
                    role="button"
                    tabIndex={0}
                    aria-label={translatedInfo["Documents missing"][props.language]}
                    title={translatedInfo["Documents missing"][props.language]}
                    onClick={handleWarningClick}
                    onKeyDown={handleWarningKeyDown}
                >
                    ⚠️
                </span>
            )}
            {showDocumentsPopup && (
                <MissingDocumentsPopup
                    language={props.language}
                    missingDocuments={missingDocuments}
                    onClose={() => setShowDocumentsPopup(false)}
                />
            )}
            {props.hideEmployee && <h2>{deviceLabel}</h2>}
            {props.hideDevice && <h2>{employee.name}</h2>}
            {!props.hideEmployee && !props.hideDevice && (
                <>
                    <h2>{employee.name}</h2>
                    <p className="assignment-card-label">{deviceLabel}</p>
                </>
            )}
            {!props.hideDevice && deviceName && <p className="assignment-card-device">{deviceName}</p>}
            <p className="assignment-card-date">{dateText}</p>
            <span className={`assignment-card-badge ${isActive ? "assignment-card-badge-active" : "assignment-card-badge-returned"}`}>
                {translatedInfo[isActive ? "Active" : "Returned"][props.language]}
            </span>
        </button>
    )
}
