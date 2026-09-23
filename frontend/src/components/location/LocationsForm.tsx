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
        image,
        setImage,
        existingImageUrl,
        imageDeleted,
        setImageDeleted
    } = props;

    const navigate = useNavigate();
    const isEditMode = backNavigationPath.includes('/locations/') && backNavigationPath !== '/locations';

    function handleImageCancel() {
        onImageCancel(setImage);
        if (existingImageUrl) {
            setImageDeleted(true);
        }
    }

    return (
        <div>
            <h2>{isEditMode ? "Edit Location" : "Add Location"}</h2>

            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    {/* Name */}
                    <label>
                        <span>Name:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </label>

                    {/* Address */}
                    <label>
                        <span>Address:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={address ?? ""}
                            onChange={(e) => setAddress(e.target.value || undefined)}
                        />
                    </label>

                    {/* Phone */}
                    <label>
                        <span>Phone:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={phone ?? ""}
                            onChange={(e) => setPhone(e.target.value || undefined)}
                        />
                    </label>

                    {/* Email */}
                    <label>
                        <span>Email:</span>
                        <input
                            className="input-small"
                            type="email"
                            value={email ?? ""}
                            onChange={(e) => setEmail(e.target.value || undefined)}
                        />
                    </label>

                    {/* Notes */}
                    <label>
                        <span>Notes:</span>
                        <textarea
                            className="input-small"
                            value={notes ?? ""}
                            onChange={(e) => setNotes(e.target.value || undefined)}
                        />
                    </label>

                    {/* Image */}
                    <label>
                        <span>Image:</span>
                        <input
                            type="file"
                            onChange={(e) => {
                                onFileChange(e, setImage);
                                setImageDeleted(false);
                            }}
                        />
                    </label>
                </div>

                {renderImagePreview(image, existingImageUrl, imageDeleted)}

                {(image || (existingImageUrl && !imageDeleted)) && (
                    <button type="button" className="button-blue margin-top-20" onClick={handleImageCancel}>
                        remove image
                    </button>
                )}

                <button type="submit" className="button-blue margin-top-50">
                    {isEditMode ? "Update Location" : "Add Location"}
                </button>
                <button type="button" className="button-blue margin-left-20" onClick={() => navigate(backNavigationPath)}>
                    back
                </button>
            </form>
        </div>
    )
}
