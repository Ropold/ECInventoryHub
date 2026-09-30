import {translatedInfo} from "../utils/TranslatedInfo.ts";
import {useNavigate} from "react-router-dom";
import {onFileChange, onImageCancel, renderImagePreview} from "../utils/ComponentsFunctions.tsx";
import "../styles/FormStyles.css";

type LocationsFormProps = {
    language: string;
    backNavigationPath: string;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    name: string;
    setName: React.Dispatch<React.SetStateAction<string>>;
    address: string | undefined;
    setAddress: React.Dispatch<React.SetStateAction<string | undefined>>;
    phone: string | undefined;
    setPhone: React.Dispatch<React.SetStateAction<string | undefined>>;
    email: string | undefined;
    setEmail: React.Dispatch<React.SetStateAction<string | undefined>>;
    notes: string | undefined;
    setNotes: React.Dispatch<React.SetStateAction<string | undefined>>;
    latitude: number | undefined;
    setLatitude: React.Dispatch<React.SetStateAction<number | undefined>>;
    longitude: number | undefined;
    setLongitude: React.Dispatch<React.SetStateAction<number | undefined>>;
    image: File | null;
    setImage: React.Dispatch<React.SetStateAction<File | null>>;
    existingImageUrl?: string;
    imageDeleted: boolean;
    setImageDeleted: React.Dispatch<React.SetStateAction<boolean>>;
}

export default function LocationsForm(props: Readonly<LocationsFormProps>) {

    const {
        backNavigationPath,
        handleSubmit,
        name,
        setName,
        address,
        setAddress,
        phone,
        setPhone,
        email,
        setEmail,
        notes,
        setNotes,
        latitude,
        setLatitude,
        longitude,
        setLongitude,
        image,
        setImage,
        existingImageUrl,
        imageDeleted,
        setImageDeleted
    } = props;

    const navigate = useNavigate();
    const isEditMode = backNavigationPath.includes('/locations/') && backNavigationPath !== '/locations';
    const hasImage = image || (existingImageUrl && !imageDeleted);

    const optionalFields = [
        {label: "Address", type: "text", value: address, setter: setAddress},
        {label: "Phone", type: "text", value: phone, setter: setPhone},
        {label: "Email", type: "email", value: email, setter: setEmail},
    ];

    // Koordinaten für die Karte, damit Mapbox die Adresse nicht jedes Mal geocodieren muss
    const coordinateFields = [
        {label: "Latitude", min: -90, max: 90, value: latitude, setter: setLatitude},
        {label: "Longitude", min: -180, max: 180, value: longitude, setter: setLongitude},
    ];

    return (
        <div>
            <h2>{isEditMode ? "Edit Location" : "Add Location"}</h2>

            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    <label>
                        <span>Name:</span>
                        <input className="input-small" type="text" value={name}
                               onChange={(e) => setName(e.target.value)} required/>
                    </label>

                    {optionalFields.map(({label, type, value, setter}) => (
                        <label key={label}>
                            <span>{label}:</span>
                            <input className="input-small" type={type} value={value ?? ""}
                                   onChange={(e) => setter(e.target.value || undefined)}/>
                        </label>
                    ))}

                    {coordinateFields.map(({label, min, max, value, setter}) => (
                        <label key={label}>
                            <span>{label}:</span>
                            <input className="input-small" type="number" step="any" min={min} max={max}
                                   value={value ?? ""}
                                   onChange={(e) => setter(e.target.value === "" ? undefined : Number(e.target.value))}/>
                        </label>
                    ))}

                    <label>
                        <span>Notes:</span>
                        <textarea className="input-small" value={notes ?? ""}
                                  onChange={(e) => setNotes(e.target.value || undefined)}/>
                    </label>

                    <label>
                        <span>Image:</span>
                        <input type="file" onChange={(e) => {
                            onFileChange(e, setImage);
                            setImageDeleted(false);
                        }}/>
                    </label>
                </div>

                {renderImagePreview(image, existingImageUrl, imageDeleted)}

                {hasImage && (
                    <button type="button" className="button-blue margin-top-20" onClick={() => {
                        onImageCancel(setImage);
                        if (existingImageUrl) setImageDeleted(true);
                    }}>
                        {translatedInfo["remove image"][props.language]}
                    </button>
                )}

                <button type="submit" className="button-blue margin-top-50">
                    {isEditMode ? translatedInfo["Update Location"][props.language] : translatedInfo["Add Location"][props.language]}
                </button>
                <button type="button" className="button-blue margin-left-20" onClick={() => navigate(backNavigationPath)}>
                    {translatedInfo["back"][props.language]}
                </button>
            </form>
        </div>
    )
}
