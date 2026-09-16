import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {useState} from "react";
import {useAutoScrollToTop} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import AssignmentCard from "./AssignmentCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type AssignmentProps = {
    language: string;
    role: string;
    assignments: AssignmentModel[];
}

function filterAssignments(assignments: AssignmentModel[], query: string): AssignmentModel[] {
    if (!assignments) return [];

    const searchQuery = query.toLowerCase();

    return assignments.filter(assignment => {
        return (
            assignment.employee.name.toLowerCase().includes(searchQuery) ||
            assignment.device.manufacturer?.toLowerCase().includes(searchQuery) ||
            assignment.device.modelName?.toLowerCase().includes(searchQuery) ||
            assignment.device.serialNumber?.toLowerCase().includes(searchQuery) ||
            assignment.device.inventoryNumber?.toLowerCase().includes(searchQuery) ||
            assignment.handedOutBy?.name.toLowerCase().includes(searchQuery) ||
            assignment.assignedDate.toLowerCase().includes(searchQuery) ||
            assignment.notes?.toLowerCase().includes(searchQuery) ||
            assignment.id.toLowerCase().includes(searchQuery)
        );
    });
}

export default function Assignments(props:Readonly<AssignmentProps>){
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useState<string>("");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/assignments/add-new-assignment`);
    }


    const filteredAssignments = filterAssignments(props.assignments, searchQuery);

    return(
        <>
            <h2>Assignments</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                />
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Assignment"][props.language]}</button>
            </div>

            {showNoPermission && (
                <NoPermissionPopup
                    language={props.language}
                    onClose={() => setShowNoPermission(false)}
                />
            )}


            <div className="assignment-card-container">
                {filteredAssignments.map((assignment) => (
                    <AssignmentCard key={assignment.id} assignment={assignment} language={props.language} />
                ))}
            </div>
        </>
    )
}
