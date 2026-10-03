import type {LocationModel} from "../models/LocationModel.ts";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/location/LocationCard.css";
import "../styles/CardBadge.css";

type LocationCardProps = {
    location: LocationModel;
    language: string;
    // Ohne Handler ist der jeweilige Pfeil ausgegraut; ohne beide werden keine Pfeile angezeigt
    onMoveUp?: () => void;
    onMoveDown?: () => void;
}

// 6 Nachkommastellen entsprechen ca. 10 cm – genauer braucht es auf der Karte nicht
function formatCoordinate(value: number | null | undefined): string {
    return value == null ? "—" : value.toFixed(6);
}

type MoveArrowProps = {
    symbol: string;
    label: string;
    onMove?: () => void;
}

// Pfeil innerhalb der Karte (selbst ein Button) – Klick/Taste darf die Karte nicht öffnen
function MoveArrow(props: Readonly<MoveArrowProps>) {
    const disabled = !props.onMove;

    function handleClick(e: React.MouseEvent) {
        e.stopPropagation();
        props.onMove?.();
    }

    function handleKeyDown(e: React.KeyboardEvent) {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            e.stopPropagation();
            props.onMove?.();
        }
    }

    return (
        <span
            className={`card-move-arrow${disabled ? " card-move-arrow-disabled" : ""}`}
            role="button"
            tabIndex={disabled ? -1 : 0}
            aria-label={props.label}
            aria-disabled={disabled}
            title={props.label}
            onClick={handleClick}
            onKeyDown={handleKeyDown}
        >
            {props.symbol}
        </span>
    );
}

export default function LocationCard(props: Readonly<LocationCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/locations/${props.location.id}`);
    }

    const showMoveArrows = Boolean(props.onMoveUp || props.onMoveDown);

    return (
        <button type="button" className="location-card" onClick={handleCardClick}>
            {showMoveArrows && (
                <div className="card-move">
                    <MoveArrow symbol="▲" label={translatedInfo["Move up"][props.language]} onMove={props.onMoveUp} />
                    <MoveArrow symbol="▼" label={translatedInfo["Move down"][props.language]} onMove={props.onMoveDown} />
                </div>
            )}
            <h2 className="card-title">{props.location.name}</h2>
            <p className="card-subtitle">{props.location.address ?? "—"}</p>
            <p className="card-detail">
                {translatedInfo["Latitude"][props.language]}: {formatCoordinate(props.location.latitude)}
            </p>
            <p className="card-detail">
                {translatedInfo["Longitude"][props.language]}: {formatCoordinate(props.location.longitude)}
            </p>
            {props.location.imageUrl && (
                <img
                    className="location-card-image"
                    src={props.location.imageUrl}
                    alt={props.location.name}
                />
            )}
        </button>
    )
}
