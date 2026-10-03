import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {useNavigate} from "react-router-dom";
import CardWarning from "../CardWarning.tsx";
import {formatDate, getDeviceLabel} from "../utils/ComponentsFunctions.tsx";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import {deviceTypeIcons} from "../utils/DeviceFilters.ts";
import "../styles/assignment/AssignmentCard.css";
import "../styles/CardBadge.css";

type AssignmentCardProps = {
    assignment: AssignmentModel;
    language: string;
    hideEmployee?: boolean;
    hideDevice?: boolean;
}

export default function AssignmentCard(props: Readonly<AssignmentCardProps>){
    const navigate = useNavigate();

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
                <CardWarning
                    language={props.language}
                    title={translatedInfo["Documents missing"][props.language]}
                    items={missingDocuments}
                />
            )}
            {props.hideEmployee && <h2 className="card-title">{deviceLabel}</h2>}
            {props.hideDevice && <h2 className="card-title">{employee.name}</h2>}
            {!props.hideEmployee && !props.hideDevice && (
                <>
                    <h2 className="card-title">{employee.name}</h2>
                    <p className="assignment-card-label">{deviceLabel}</p>
                </>
            )}
            {!props.hideDevice && deviceName && <p className="card-subtitle">{deviceName}</p>}
            <p className="assignment-card-date">{dateText}</p>
            <span className={`card-badge ${isActive ? "card-badge-green" : "card-badge-grey"}`}>
                {translatedInfo[isActive ? "Active" : "Returned"][props.language]}
            </span>
        </button>
    )
}
