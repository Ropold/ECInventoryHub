import {translatedInfo} from "../utils/TranslatedInfo.ts";
import {useNavigate} from "react-router-dom";
import type {Department} from "../models/EmployeeModel.ts";
import {onFileChange, onImageCancel, renderImagePreview} from "../utils/ComponentsFunctions.tsx";
import "../styles/FormStyles.css";

type EmployeeFormProps = {
    language: string;
    backNavigationPath: string;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    personnelNumber: string | undefined;
    setPersonnelNumber: React.Dispatch<React.SetStateAction<string | undefined>>;
    name: string;
    setName: React.Dispatch<React.SetStateAction<string>>;
    email: string | undefined;
    setEmail: React.Dispatch<React.SetStateAction<string | undefined>>;
    phone: string | undefined;
    setPhone: React.Dispatch<React.SetStateAction<string | undefined>>;
    address: string | undefined;
    setAddress: React.Dispatch<React.SetStateAction<string | undefined>>;
    department: Department;
    setDepartment: React.Dispatch<React.SetStateAction<Department>>;
    active: boolean;
    setActive: React.Dispatch<React.SetStateAction<boolean>>;
    notes: string | undefined;
    setNotes: React.Dispatch<React.SetStateAction<string | undefined>>;
    image: File | null;
    setImage: React.Dispatch<React.SetStateAction<File | null>>;
    existingImageUrl?: string;
    imageDeleted: boolean;
    setImageDeleted: React.Dispatch<React.SetStateAction<boolean>>;
}

export default function EmployeeForm(props: Readonly<EmployeeFormProps>) {

    const {
        backNavigationPath,
        handleSubmit,
        personnelNumber,
        setPersonnelNumber,
        name,
        setName,
        email,
        setEmail,
        phone,
        setPhone,
        address,
        setAddress,
        department,
        setDepartment,
        active,
        setActive,
        notes,
        setNotes,
        image,
        setImage,
        existingImageUrl,
        imageDeleted,
        setImageDeleted
    } = props;

    const navigate = useNavigate();
    const isEditMode = backNavigationPath.includes('/employees/') && backNavigationPath !== '/employees';

    function handleImageCancel() {
        onImageCancel(setImage);
        if (existingImageUrl) {
            setImageDeleted(true);
        }
    }

    return (
        <div>
            <h2>{isEditMode ? translatedInfo["Edit Employee"][props.language] : translatedInfo["Add Employee"][props.language]}</h2>

            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    {/* Name */}
                    <label>
                        <span>{translatedInfo["Name"][props.language]}:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </label>

                    {/* Personnel Number */}
                    <label>
                        <span>{translatedInfo["Personnel Number"][props.language]}:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={personnelNumber ?? ""}
                            onChange={(e) => setPersonnelNumber(e.target.value || undefined)}
                        />
                    </label>

                    {/* Department */}
                    <label>
                        <span>{translatedInfo["Department"][props.language]}:</span>
                        <select
                            className="input-small"
                            value={department}
                            onChange={(e) => setDepartment(e.target.value as Department)}
                            required
                        >
                            <option value="MARKETING">{translatedInfo["Marketing"][props.language]}</option>
                            <option value="ACCOUNTING">{translatedInfo["Accounting"][props.language]}</option>
                            <option value="HR">{translatedInfo["HR"][props.language]}</option>
                            <option value="DEVELOPMENT">{translatedInfo["Development"][props.language]}</option>
                            <option value="MANAGEMENT">{translatedInfo["Management"][props.language]}</option>
                        </select>
                    </label>

                    {/* Active */}
                    <label>
                        <span>{translatedInfo["Active"][props.language]}:</span>
                        <input
                            type="checkbox"
                            checked={active}
                            onChange={(e) => setActive(e.target.checked)}
                        />
                    </label>

                    {/* Email */}
                    <label>
                        <span>{translatedInfo["Email"][props.language]}:</span>
                        <input
                            className="input-small"
                            type="email"
                            value={email ?? ""}
                            onChange={(e) => setEmail(e.target.value || undefined)}
                        />
                    </label>

                    {/* Phone */}
                    <label>
                        <span>{translatedInfo["Phone"][props.language]}:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={phone ?? ""}
                            onChange={(e) => setPhone(e.target.value || undefined)}
                        />
                    </label>

                    {/* Address */}
                    <label>
                        <span>{translatedInfo["Address"][props.language]}:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={address ?? ""}
                            onChange={(e) => setAddress(e.target.value || undefined)}
                        />
                    </label>

                    {/* Notes */}
                    <label>
                        <span>{translatedInfo["Notes"][props.language]}:</span>
                        <textarea
                            className="input-small"
                            value={notes ?? ""}
                            onChange={(e) => setNotes(e.target.value || undefined)}
                        />
                    </label>

                    {/* Image */}
                    <label>
                        <span>{translatedInfo["Image"][props.language]}:</span>
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
                    <button type="button" className="button-blue margin-top-20 margin-right-10" onClick={handleImageCancel}>
                        {translatedInfo["remove image"][props.language]}
                    </button>
                )}

                <button type="submit" className="button-blue margin-top-50">
                    {isEditMode ? translatedInfo["Update Employee"][props.language] : translatedInfo["Add Employee"][props.language]}
                </button>
                <button type="button" className="button-blue margin-left-20" onClick={() => navigate(backNavigationPath)}>
                    {translatedInfo["back"][props.language]}
                </button>
            </form>
        </div>
    )
}
