import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {type DeviceTypeFilter, deviceTypeFilters} from "../utils/DeviceFilters.ts";
import {useState} from "react";
import {useAutoScrollToTop, useSessionState} from "../utils/ComponentsFunctions.tsx";
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

type StatusFilter = "ACTIVE" | "RETURNED" | "ALL";
type TypeFilter = DeviceTypeFilter;

const statusFilters: {value: StatusFilter; labelKey: string}[] = [
    {value: "ACTIVE", labelKey: "Active"},
    {value: "RETURNED", labelKey: "Returned"},
    {value: "ALL", labelKey: "All"},
];

const typeFilters = deviceTypeFilters;

// "ALL" oder die ID des ausgebenden Mitarbeiters
type HandedOutByFilter = string;

// Alle Mitarbeiter, die schon mindestens eine Zuweisung ausgegeben haben, alphabetisch
function getHandedOutByOptions(assignments: AssignmentModel[]): {id: string; name: string}[] {
    const byId = new Map<string, string>();
    assignments.forEach((assignment) => {
        if (assignment.handedOutBy) {
            byId.set(assignment.handedOutBy.id, assignment.handedOutBy.name);
        }
    });
    return [...byId.entries()]
        .map(([id, name]) => ({id, name}))
        .sort((a, b) => a.name.localeCompare(b.name));
}

function filterAssignments(
    assignments: AssignmentModel[],
    query: string,
    statusFilter: StatusFilter,
    typeFilter: TypeFilter,
    handedOutByFilter: HandedOutByFilter
): AssignmentModel[] {
    if (!assignments) return [];

    const searchQuery = query.toLowerCase();

    return assignments
        .filter(assignment => {
            if (statusFilter === "ACTIVE" && assignment.returnedDate) return false;
            if (statusFilter === "RETURNED" && !assignment.returnedDate) return false;
            if (typeFilter !== "ALL" && assignment.device.type !== typeFilter) return false;
            if (handedOutByFilter !== "ALL" && assignment.handedOutBy?.id !== handedOutByFilter) return false;
            // Bewusst ohne handedOutBy: Ein Name findet nur die Zuweisungen, die die Person selbst hat.
            // Wer etwas ausgegeben hat, filtert man über "Ausgegeben von".
            return (
                assignment.employee.name.toLowerCase().includes(searchQuery) ||
                assignment.device.manufacturer?.toLowerCase().includes(searchQuery) ||
                assignment.device.modelName?.toLowerCase().includes(searchQuery) ||
                assignment.device.serialNumber?.toLowerCase().includes(searchQuery) ||
                assignment.device.inventoryNumber?.toLowerCase().includes(searchQuery) ||
                assignment.device.hostname?.toLowerCase().includes(searchQuery) ||
                assignment.assignedDate.toLowerCase().includes(searchQuery) ||
                assignment.notes?.toLowerCase().includes(searchQuery) ||
                assignment.id.toLowerCase().includes(searchQuery)
            );
        })
        // Neueste zuerst
        .sort((a, b) => b.assignedDate.localeCompare(a.assignedDate));
}

export default function Assignments(props:Readonly<AssignmentProps>){
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useSessionState<string>("assignments.searchQuery", "");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);
    const [statusFilter, setStatusFilter] = useSessionState<StatusFilter>("assignments.statusFilter", "ACTIVE");
    const [typeFilter, setTypeFilter] = useSessionState<TypeFilter>("assignments.typeFilter", "ALL");
    const [handedOutByFilter, setHandedOutByFilter] = useSessionState<HandedOutByFilter>("assignments.handedOutByFilter", "ALL");

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/assignments/add-new-assignment`);
    }


    const handedOutByOptions = getHandedOutByOptions(props.assignments);
    const handedOutByLabel = translatedInfo["Handed Out By"][props.language];
    const filteredAssignments = filterAssignments(props.assignments, searchQuery, statusFilter, typeFilter, handedOutByFilter);

    return(
        <>
            <h2>{translatedInfo["Assignments"][props.language]}</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    language={props.language}
                    hasActiveFilters={statusFilter !== "ACTIVE" || typeFilter !== "ALL" || handedOutByFilter !== "ALL"}
                    onReset={() => {
                        setStatusFilter("ACTIVE");
                        setTypeFilter("ALL");
                        setHandedOutByFilter("ALL");
                    }}
                />
                <select
                    className="filter-select"
                    value={typeFilter}
                    onChange={(e) => setTypeFilter(e.target.value as TypeFilter)}
                >
                    {typeFilters.map((filter) => (
                        <option key={filter.value} value={filter.value}>
                            {translatedInfo[filter.labelKey][props.language]}
                        </option>
                    ))}
                </select>
                <select
                    className="filter-select"
                    value={handedOutByFilter}
                    onChange={(e) => setHandedOutByFilter(e.target.value)}
                    aria-label={handedOutByLabel}
                >
                    <option value="ALL">{handedOutByLabel}: {translatedInfo["All"][props.language]}</option>
                    {handedOutByOptions.map((option) => (
                        <option key={option.id} value={option.id}>
                            {handedOutByLabel}: {option.name}
                        </option>
                    ))}
                </select>
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Assignment"][props.language]}</button>
            </div>

            <div className="filter-row">
                <div className="filter-toggle" role="group">
                    {statusFilters.map((filter) => (
                        <button
                            key={filter.value}
                            type="button"
                            className={statusFilter === filter.value ? "button-blue" : "button-grey"}
                            aria-pressed={statusFilter === filter.value}
                            onClick={() => setStatusFilter(filter.value)}
                        >
                            {translatedInfo[filter.labelKey][props.language]}
                        </button>
                    ))}
                </div>
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
